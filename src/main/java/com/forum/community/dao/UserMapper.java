package com.forum.community.dao;

import com.forum.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") int id);

    User selectByName(@Param("username") String username);

    User selectByEmail(@Param("email") String email);

    int insertUser(@Param("user") User user);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updatePassword(@Param("id") int id, @Param("password") String password);

}

