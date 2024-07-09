//package com.example.webchat.utils;
//
//import com.example.webchat.enums.RoleName;
//import com.example.webchat.enums.RoomType;
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
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private RoomService roomService;
//    private final Faker faker = new Faker();
//
//    @Override
//    public void run(String... args) {
//        createUsersWithRole(2, RoleName.ADMIN);
//        createUsersWithRole(5, RoleName.TEACHER);
//        createUsersWithRole(20, RoleName.STUDENT);
//        createPublicRooms(3);
//        createGroupRooms(5);
//        createPersonalRooms(10);
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
//    private void createPublicRooms(int count) {
//        List<User> allUsers = userService.getAllUsers();
//        List<User> teachers = allUsers.stream()
//                .filter(user -> user.getRole() == RoleName.TEACHER)
//                .collect(Collectors.toList());
//
//        for (int i = 0; i < count; i++) {
//            Room room = new Room();
//            room.setName("Public: " + faker.educator().course());
//            room.setDescription(faker.lorem().sentence());
//            room.setPrivate(false);
//            User creator = teachers.get(faker.random().nextInt(teachers.size()));
//            List<String> userIds = allUsers.stream()
//                    .filter(user -> faker.random().nextBoolean())
//                    .map(user -> user.getId().toString())
//                    .collect(Collectors.toList());
//            room.setUserIds(userIds);
//            room.setRoomType(RoomType.PUBLIC);
//            roomService.createRoom(room, creator.getUsername());
//        }
//    }
//
//    private void createGroupRooms(int count) {
//        List<User> allUsers = userService.getAllUsers();
//        List<User> teachers = allUsers.stream()
//                .filter(user -> user.getRole() == RoleName.TEACHER)
//                .collect(Collectors.toList());
//
//        for (int i = 0; i < count; i++) {
//            Room room = new Room();
//            room.setName("Group: " + faker.team().name());
//            room.setDescription(faker.lorem().sentence());
//            room.setPrivate(true);
//            User creator = teachers.get(faker.random().nextInt(teachers.size()));
//            List<String> userIds = allUsers.stream()
//                    .filter(user -> faker.random().nextBoolean())
//                    .limit(faker.number().numberBetween(3, 10))
//                    .map(user -> user.getId().toString())
//                    .collect(Collectors.toList());
//            room.setUserIds(userIds);
//            room.setRoomType(RoomType.GROUP);
//            roomService.createRoom(room, creator.getUsername());
//        }
//    }
//
//    private void createPersonalRooms(int count) {
//        List<User> allUsers = userService.getAllUsers();
//
//        for (int i = 0; i < count; i++) {
//            Room room = new Room();
//            room.setName("Personal Chat");
//            room.setDescription("Direct message between two users");
//            room.setPrivate(true);
//            List<User> participants = faker.random().nextBoolean() ?
//                    selectRandomUsers(allUsers, 2) :
//                    selectTeacherAndStudent(allUsers);
//            List<String> userIds = participants.stream()
//                    .map(user -> user.getId().toString())
//                    .collect(Collectors.toList());
//            room.setUserIds(userIds);
//            room.setRoomType(RoomType.PERSONAL);
//            roomService.createRoom(room, participants.get(0).getUsername());
//        }
//    }
//
//    private List<User> selectRandomUsers(List<User> users, int count) {
//        List<User> shuffledUsers = new ArrayList<>(users);
//        Collections.shuffle(shuffledUsers, new Random());
//        return shuffledUsers.subList(0, Math.min(count, shuffledUsers.size()));
//    }
//
//    private List<User> selectTeacherAndStudent(List<User> users) {
//        User teacher = users.stream()
//                .filter(user -> user.getRole() == RoleName.TEACHER)
//                .collect(Collectors.toList())
//                .get(faker.random().nextInt(5));
//
//        User student = users.stream()
//                .filter(user -> user.getRole() == RoleName.STUDENT)
//                .collect(Collectors.toList())
//                .get(faker.random().nextInt(20));
//
//        return Arrays.asList(teacher, student);
//    }
//}
//
//
