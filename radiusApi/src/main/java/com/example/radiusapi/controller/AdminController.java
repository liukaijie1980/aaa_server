    package com.example.radiusapi.controller;
    import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
    import com.baomidou.mybatisplus.core.metadata.IPage;
    import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
    import com.example.radiusapi.entity.Admin;
    import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
    import com.example.radiusapi.mapper.AdminMapper;
    import com.example.radiusapi.utils.GjpLogger;
    import com.example.radiusapi.utils.TokenUtil;

    import io.swagger.v3.oas.annotations.Operation;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.dao.DataAccessException;
    import org.springframework.util.MultiValueMap;
    import org.springframework.web.bind.annotation.*;
    import com.example.radiusapi.utils.Result;
    import java.util.List;
    import java.util.Map;
    import static com.example.radiusapi.utils.TokenUtil.sign;


    @RestController
    @RequestMapping("/admin")
    @CrossOrigin
    @Slf4j
    public class AdminController
    {
        @Autowired
        private AdminMapper Mapper;


        @Operation(summary = "Login, input the username and password to get token")
        @PostMapping("/login")
        public Result login(@RequestBody Map admin) {

            log.info("login({})",admin);
            String Name = admin.get("username").toString();
            String Password = admin.get("password").toString();


            QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>();

            wrapper.eq("name", Name)
                    .eq("password", Password);
            Result ret=new Result();
            List<Admin> dataSet ;
            try {
                dataSet= Mapper.selectList(wrapper);
            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
                ret.error();
                return  ret;
            }
            if (dataSet.size() > 0) {
                //String token= JwtUtils.generateToken(Name);
                String token = sign(Name);
                ret.ok();
                ret.data("token", token);
            } else {
                ret.error();
                ret.setMessage("账号或密码不正确");
            }
            log.info("result={}",ret);
            return ret;
        }

        @Operation(summary = "input token to get infomation （such as role ）for the adminstrator")
        @GetMapping("/info")
            public Result info(String  token){
            log.info("info({})", token);
            //String NameInToken=JwtUtils.getClaimsByToken(token).getSubject();
            String NameInToken= TokenUtil.getNamebyToken(token);
            GjpLogger.debug("NameInToken="+NameInToken);

            QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>();

            wrapper.eq("name", NameInToken);
            Result ret=new Result();
            List<Admin> dataSet;

            try {
                dataSet = Mapper.selectList(wrapper);
            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
                ret.error();
                return  ret;
            }
            ret.ok();
            ret.data("name",NameInToken)
               .data("roles",dataSet.get(0).getRole())
               .data("nodeid",dataSet.get(0).getNodeid())
               .data("introduction","superUser")
               .data("avatar","");
            log.info("Result={}", ret);
            return  ret;
        }

        @Operation(summary = " we will just verify the token ,but do nothing in the backend")
        @PostMapping("/logout")
        public  Result logout(@RequestHeader MultiValueMap<String, String> headers){
            log.info("logout()");
            String token=headers.get("x-token").get(0);
            String NameInToken= TokenUtil.getNamebyToken(token);
           // String NameInToken=JwtUtils.getClaimsByToken(token).getSubject();
            log.info(NameInToken);
            Result ret=new Result();
            ret.ok();
            return ret;
        }

        @Operation(summary = " input role, nodeId(sidebarTree), name(optional) to get list of  administrators ")
        @GetMapping("/administrator")
        public Result GetAdministrators(
                @RequestParam("role") String Role,
                @RequestParam("nodeid") String NodeId,
                @RequestParam("name") String Name,
                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize
        )
        {
            log.info("GetAdministrators({},{},{})",Role,NodeId,Name);

            QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>();

            wrapper.eq("nodeid", NodeId);
            if(!Role.equals("admin"))  {
                wrapper.eq("role", Role);
            }

            if (!Name.equals(""))
            {
                wrapper.eq("Name", Name);
            }
            Page<Admin> page=new Page<>(pageNo,pageSize);

            IPage<Admin> iPage;
            Result ret=new Result();

            try {
                iPage= Mapper.selectPage(page,wrapper);

            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
                ret.error();
                return  ret;
            }

            ret.ok() ;
            ret.data("data",iPage);
            log.info("result={}",ret);
            return  ret;

        }


        @Operation(summary = "add a administrator by pass a Admin Object ")
        @PostMapping("/administrator")
        public Result AddAdministrator( @RequestBody Admin admin )
        {
           // GjpLogger.info("AddAdministrator()");
             log.info("AddAdministrator({})",admin);

            int i=0;
            try {
                 i=Mapper.insert(admin);
            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
            }
            Result ret=new Result();
            if (i>0)
            {
                ret.ok();
                log.info( "Add administrator success");
            }else
            {
                ret.error();
                log.info( "Add administrator fail");
            }
            return ret;
        }

        @Operation(summary = "Update administrator information , this api can't use for modify 'name' ")
        @PutMapping("/administrator")
        public Result UpdateAdministrator(@RequestBody Admin admin)
        {
            log.info("UpdateAdministrator({})",admin);

            UpdateWrapper<Admin> wrapper= new UpdateWrapper<>();
            wrapper.eq("name",admin.getName());

            int i=0;
            try {
              i=Mapper.update(admin,wrapper);
            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
            }

            Result ret=new Result();
            if (i>0) {
                ret.ok();
                log.info( "Update administrator success");
            }else            {
                ret.error();
                log.info( "Update administrator fail");
            }

            return ret;
        }


        @Operation(summary = "delete administrator by input the name ")
        @DeleteMapping("/administrator")
        public Result DeleteAdministrator( @RequestParam("name") String Name)
        {
            log.info("DeleteAdministrator({})",Name);

            UpdateWrapper<Admin> wrapper= new UpdateWrapper<>();
            wrapper.eq("name",Name);

            int i=0;
            try {
                i=Mapper.delete(wrapper);
            } catch (DataAccessException e) {
                // 处理异常
                log.error(e.getMessage());
            }

            Result ret=new Result();
            if (i>0) {
                ret.ok();
                log.info( "delete administrator success");
            }else            {
                ret.error();
                log.error( "delete administrator fail");
            }

            return ret;
        }



    }
