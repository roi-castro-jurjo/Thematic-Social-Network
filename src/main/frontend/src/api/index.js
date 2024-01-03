import DATA from './data'

let __instance = null

export default class API {
    #token = sessionStorage.getItem('token') || null

    static instance() {
        if(__instance == null)
            __instance = new API()

        return __instance
    }


     async login(email, pass) {
        try {
            // Realiza la solicitud a la API
            const response = await fetch('http://localhost:8080/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password: pass }),
            });

            // Verifica si la solicitud fue exitosa
            if (response.ok) {
                const token = response.headers.get('Authentication'); // Asegúrate de reemplazar 'Tu-Header-De-Token' con el nombre real del header donde tu API retorna el token
                localStorage.setItem('user', email);
                localStorage.setItem('token', token);
                this.#token = token;
                return true;
            } else {
                // Manejo de errores (por ejemplo, credenciales incorrectas)
                return false;
            }
        } catch (error) {
            // Manejo de error de la red o solicitud fallida
            console.error('Error al realizar la solicitud:', error);
            return false;
        }
    }
    async logout() {
        this.#token = null
        localStorage.clear()

        return true
    }
    async findMovies(
        {
            filter: { genre = '', title = '', status = '' } = { genre : '', title : '', status : '' },
            sort,
            pagination: {page = 0, size = 7} = { page: 0, size: 7 }
        } = {
            filter: { genre : '', title : '', status : '' },
            sort: {},
            pagination: { page: 0, size: 7 }
        }
    ) {
        return new Promise(resolve => {
            const filtered = DATA.movies
                ?.filter(movie => movie.title.toLowerCase().includes(title.toLowerCase() || ''))
                ?.filter(movie => genre !== '' ? movie.genres.map(genre => genre.toLowerCase()).includes(genre.toLowerCase()) : true)
                ?.filter(movie => movie.status.toLowerCase().includes(status.toLowerCase() || ''))

            const data = {
                content: filtered?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < filtered.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }
    async findMovie(id) {
        return DATA.movies.find(movie => movie.id === id)
    }
    async findUser(email) {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                console.error('Authentication token not found');
                return null;
            }

            const response = await fetch(`http://localhost:8080/users/${email}`, {
                headers: {
                    'Authorization': `${token}`,
                },
            });

            switch (response.status) {
                case 200:
                    return await response.json();

                case 401:
                    console.error('Invalid authentication token');
                    return null;

                case 403:
                    console.error('Insufficient permissions to access user details');
                    return null;

                case 404:
                    console.error('User not found');
                    return null;

                default:
                    console.error('Error while making the request');
                    return null;
            }
        } catch (error) {
            console.error('Error while making the request:', error);
            return null;
        }
    }




    async findComments(
        {
            filter: { movie = '', user = '' } = { movie: '', user: '' },
            sort,
            pagination: {page = 0, size = 10} = { page: 0, size: 10}
        } = {
            filter: { movie: '', user: '' },
            sort: {},
            pagination: { page: 0, size: 10}
        }
    ) {
        return new Promise(resolve => {
            const filtered = DATA.comments
                ?.filter(comment => comment?.movie?.id === movie)

            const data = {
                content: filtered?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < filtered.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }

    async createComment(comment) {
        return new Promise(resolve => {
            DATA.comments.unshift(comment)

            resolve(true)
        })
    }

    async createUser(user) {
        console.log(user)
    }

    async updateUser(id, user) {
        console.log(user)
    }
}