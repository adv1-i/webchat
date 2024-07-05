//package com.example.webchat;
//
//import com.example.webchat.enums.RoleName;
//import com.example.webchat.enums.UserStatus;
//import com.example.webchat.model.Room;
//import com.example.webchat.model.User;
//import com.example.webchat.service.RoomService;
//import com.example.webchat.service.UserService;
//import com.github.javafaker.Faker;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private RoomService roomService;
//
//    private final Faker faker = new Faker();
//
//    @Override
//    public void run(String... args) {
//        createUsersWithRole(2, RoleName.ADMIN);
//
//        createUsersWithRole(5, RoleName.TEACHER);
//
//        createUsersWithRole(20, RoleName.STUDENT);
//
//        createRooms(5);
//    }
//
//    private void createUsersWithRole(int count, RoleName role) {
//        for (int i = 0; i < count; i++) {
//            User user = new User();
//            user.setUsername(faker.name().username());
//            user.setPassword("password");
//            user.setRole(role);
//            user.setStatus(UserStatus.OFFLINE);
//            userService.createUser(user);
//        }
//    }
//
//    private void createRooms(int count) {
//        List<User> allUsers = userService.getAllUsers();
//        List<User> teachers = allUsers.stream()
//                .filter(user -> user.getRole() == RoleName.TEACHER)
//                .collect(Collectors.toList());
//
//        for (int i = 0; i < count; i++) {
//            Room room = new Room();
//            room.setName(faker.educator().course());
//            room.setDescription(faker.lorem().sentence());
//            room.setPrivate(faker.bool().bool());
//
//            User creator = teachers.get(faker.random().nextInt(teachers.size()));
//
//            List<String> userIds = allUsers.stream()
//                    .filter(user -> faker.random().nextBoolean())
//                    .map(user -> user.getId().toString())
//                    .collect(Collectors.toList());
//            room.setUserIds(userIds);
//
//            roomService.createRoom(room, creator.getUsername());
//        }
//    }
//}
//
//
