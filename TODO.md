# TODO:
## Movies
- [x] Get Movie by Id
- [ ] Get All Movies:
  - [x] Paginated
  - [x] Return only certain attributes
  - [ ] Filter by:
    - [x] Keywords
    - [x] Genre
    - [ ] Credits
    - [ ] Release Date
  - [ ] Sort by:
    - [ ] Release Date
    - [x] Title
- [x] Create new Movie:
  - [x] Only Title mandatory
- [x] Modifie Movie
- [x] Delete Movie

## Users
- [x] Get a User by Id
- [x] Get All Users:
  - [x] Paginated
  - [x] Return only certain atributes
  - [x] Filter by:
      - [x] Name
      - [x] Email
  - [x] Sort by any attribute
- [x] Create new User:
  - [x] Email mandatory
  - [x] Name mandatory
  - [x] Birthday mandatory
- [x] Delete User
- [x] Modifie User:
  - [x] Email and Birthday cannot be modified
- [x] Add Friend
- [x] Delete Friend

## COMMENTS
- [x] Get all Comments of Movie
- [x] Get all Comments from User
- [x] Add new Comment to Movie
- [x] Edit Comment
- [x] Delete Comment

## FIXES 
- [x] Fix how you search for comments based on movie and user
- [x] Clean URLs to search for movies
- [x] Clean URLs to search for users
- [ ] Make that deleting a movie deletes all related comments
- [ ] Make that deleting an user sets all comments of that user to "User deleted"
- [ ] Make the ready console log more professional
- [ ] Remodel all friendship system:
  - [ ] Make it use the friendship class
  - [ ] Make it not bidirectional until confirmed
  - [ ] Make it bidirectional when confirmed


## AUTHORITATION
- [x] Get a single user
- [x] Get all users
- [x] Create a new user
- [x] Delete a user
- [x] Modifie a user
- [ ] Add a friend
- [ ] Delete a friend
- [x] Get a film
- [x] Get all films
- [x] Create a new film
- [x] Modifie a film
- [x] Delete a film
- [x] Get all comments from a film
- [x] Get all comments from a user
- [ ] Add comment to a film
- [ ] Edit a comment
- [ ] Delete a comment

## HATEOAS
- [x] Get a user
- [x] Get all users
- [x] Create a new user
- [x] Delete a user
- [x] Edit a user
- [x] Get a film
- [x] Get all films
- [x] Create a film
- [x] Delete a film
- [x] Modifie a film
- [x] Get all comments from a film
- [x] Get all comments from a user
- [ ] Add comment to a film
- [ ] Edit a comment
- [ ] Delete a comment

## SWAGGER
- [x] Get a user
- [x] Get all users
- [x] Create a new user
- [x] Delete a user
- [x] Edit a user
- [x] Get a film
- [x] Get all films
- [x] Create a film
- [x] Delete a film
- [x] Modifie a film
- [x] Get all comments from a film
- [x] Get all comments from a user
- [ ] Add comment to a film
- [ ] Edit a comment
- [ ] Delete a comment


- [ ] Fix Links when adding
- [ ] Fix swagger documentation to movies and assessments
- [ ] Fix friendship confirm and swagger docs