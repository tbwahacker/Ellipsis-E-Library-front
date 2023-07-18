package com.anga.Elipsis_LMS;

public class Constant {
    public static final String URL = "http://192.168.46.105:8000/";
    public static final String HOME = URL+"api";
    public static final String LOGIN = HOME+"/login";
    public static final String LOGOUT = HOME+"/logout";
    public static final String REGISTER = HOME+"/register";
    public static final String BOOKS = HOME+"/books";
    public static final String ADD_BOOK = BOOKS +"/create";
    public static final String UPDATE_BOOK = BOOKS +"/update";
    public static final String DELETE_BOOK = BOOKS +"/delete";
    public static final String LIKE_BOOK = BOOKS +"/like";
    public static final String COMMENTS = BOOKS +"/comments";
    public static final String CREATE_COMMENT = HOME+"/comments/create";
    public static final String DELETE_COMMENT = HOME+"/comments/delete";
    public static final String ADD_USER = HOME+"/users/create";
    public static final String UPDATE_USER = HOME+"/users/update";
    public static final String ALL_USER = HOME+"/users";
    public static final String DELETE_USER = HOME+"/users/delete";
}
