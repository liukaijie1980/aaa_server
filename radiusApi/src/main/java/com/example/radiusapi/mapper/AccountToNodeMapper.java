package com.example.radiusapi.mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountToNodeMapper {
    @Select("SELECT DISTINCT r.node_id FROM radius.account_info AS a JOIN radius.realm AS r ON a.realm = r.realm WHERE a.user_name LIKE #{username}")
    List<String> findDistinctNodeIdsByUsername(@Param("username") String username);
}

