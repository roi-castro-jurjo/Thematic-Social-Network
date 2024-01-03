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
    async findMovies({
                         page = 0,
                         size = 7,
                         order = '',
                         genre = '',
                         keyword = '',
                         credits = '',
                         date = ''
                     } = {}) {
        try {
            const token = localStorage.getItem('token');

            if (!token) {
                console.error('Authentication token not found');
                return null;
            }

            // Construye los parámetros de consulta solo con valores no vacíos
            const queryParams = new URLSearchParams({ page, size });
            if (order) queryParams.append('order', order);
            if (genre) queryParams.append('genre', genre);
            if (keyword) queryParams.append('keyword', keyword);
            if (credits) queryParams.append('credits', credits);
            if (date) queryParams.append('date', date);

            const response = await fetch(`http://localhost:8080/movies?${queryParams}`, {
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
                    console.error('Insufficient permissions to access movies list');
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


    async findMovie(id) {
        try {
            const token = localStorage.getItem('token');

            if (!token) {
                console.error('Authentication token not found');
                return null;
            }

            const response = await fetch(`http://localhost:8080/movies/${id}`, {
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
                    console.error('Insufficient permissions to access movie details');
                    return null;

                case 404:
                    console.error('Movie not found');
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