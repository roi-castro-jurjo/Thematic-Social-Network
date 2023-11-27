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
- [ ] Clean URLs to search for users
- [ ] Make that deleting a movie deletes all related comments
- [ ] Make that deleting an user sets all comments of that user to "User deleted"
- [ ] Make the ready console log more professional
- [ ] Remodel all friendship system:
  - [ ] Make it use the friendship class
  - [ ] Make it not bidirectional until confirmed
  - [ ] Make it bidirectional when confirmed