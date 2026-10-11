module com.example.badminton {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.badminton to javafx.fxml;
    opens com.example.badminton.controller to javafx.fxml;
    opens com.example.badminton.model to javafx.base;

    exports com.example.badminton;
    exports com.example.badminton.model;
    exports com.example.badminton.service;
    exports com.example.badminton.controller;
}