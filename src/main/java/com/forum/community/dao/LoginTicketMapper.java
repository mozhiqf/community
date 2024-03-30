package com.forum.community.dao;

import com.forum.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired)" +
                    "values(#{userId},#{ticket},#{status},#{expired})"
    })
    public int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired " +
                    "from login_ticket where ticket=#{ticket}"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "</script>"
    })
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);

}
