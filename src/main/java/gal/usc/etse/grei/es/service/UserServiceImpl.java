package gal.usc.etse.grei.es.service;

import gal.usc.etse.grei.es.domain.User;
import gal.usc.etse.grei.es.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }
    

    public Optional<User> get(String id) {
        return userRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAll(Query query) {
        return userRepository.findAll(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<User> getPage(Query query, Pageable pageable) {
        return userRepository.findAll(query, pageable);
    }

    @Override
    public User postUser(User user) {
        if (user.getName() == null || Objects.equals(user.getName(), "")) {
            System.out.println("Name is mandatory to POST.");
            return null;
        }
        if (user.getEmail() == null || Objects.equals(user.getEmail(), "")) {
            System.out.println("Email is mandatory to POST.");
            return null;
        }
        if (user.getBirthday() == null) {
            System.out.println("Birthday is mandatory to POST.");
            return null;
        }

        if (userRepository.findById(user.getEmail()).isPresent()) {
            System.out.println("A user with this email already exists.");
            return null;
        }
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    private void updateUserAttributes(User movieToUpdate, User movie) {
        Class<?> clazz = movie.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(movie);
                if (value != null) {
                    Field fieldToUpdate = clazz.getDeclaredField(field.getName());
                    fieldToUpdate.setAccessible(true);
                    fieldToUpdate.set(movieToUpdate, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public User putUser(String id, User user){
        Optional<User> currentUserOptional = userRepository.findById(id);
        if (currentUserOptional.isPresent()) {
            User currentUser = currentUserOptional.get();

            if ((user.getEmail() != null && !user.getEmail().equals(currentUser.getEmail())) || (user.getBirthday() != null && !user.getBirthday().equals(currentUser.getBirthday()))) {
                return null;
            }

            updateUserAttributes(currentUser, user);
            return userRepository.save(currentUser);
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteUserById(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> getByName(String name){
        return userRepository.findByName(name);
    }

    @Override
    public User addFriend(String userEmail, User friend) {
        Optional<User> userOptional = userRepository.findById(userEmail);
        if (!userOptional.isPresent()) {
            return null;
        }

        Optional<User> friendOptional = userRepository.findById(friend.getEmail());
        if (!friendOptional.isPresent()) {
            return null;
        }

        User user = userOptional.get();
        User newFriend = friendOptional.get();

        boolean friendAlreadyAdded = user.getFriends() != null && user.getFriends().stream()
                .anyMatch(f -> f.getEmail().equals(newFriend.getEmail()));

        if (friendAlreadyAdded) {
            return user;
        } else {
            if (user.getFriends() == null) {
                user.setFriends(new ArrayList<>());
            }
            user.getFriends().add(newFriend);
            userRepository.save(user);
            if (newFriend.getFriends() == null){
                newFriend.setFriends(new ArrayList<>());
            }
            if (!newFriend.getFriends().contains(user)){
                this.addFriend(newFriend.getEmail(), user);
            }
            return userRepository.save(user);
        }
    }

    @Transactional
    @Override
    public boolean removeFriend(String userEmail, String friendEmail) {
        Optional<User> userOptional = userRepository.findById(userEmail);
        if (!userOptional.isPresent()) {
            return false;
        }

        User user = userOptional.get();
        List<User> friends = user.getFriends();
        if (friends == null || friends.isEmpty()) {
            return false;
        }

        Optional<User> friendOptional = userRepository.findById(friendEmail);
        if (!friendOptional.isPresent()) {
            return false;
        }

        User friendUser = friendOptional.get();

        boolean isRemoved = friends.removeIf(friend -> friend.getEmail().equals(friendEmail));

        if (isRemoved) {
            userRepository.save(user);
            friendUser.getFriends().removeIf(f -> f.getEmail().equals(userEmail));
            userRepository.save(friendUser);
        }

        return isRemoved;
    }






}