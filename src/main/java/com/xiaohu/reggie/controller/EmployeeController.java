package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.entity.Employee;
import com.xiaohu.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /*
     * 员工登录
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1.将页面提交的密码password进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3 如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }
        // 4 密码对比 如果不一致 则返回登陆失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败");
        }

        // 5 查看员工状态，如果为 已禁用状态，则返回已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //6 登录成功， 将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /*
     * 员工退出
     * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //新增员工
    @PostMapping
    public R<String> saveEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工,员工信息{}", employee.toString());
        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        log.info("新增员工,员工信息{}", employee.toString());
        employeeService.save(employee);
        return R.success("新增用户成功");
    }


    //分页功能开发
    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        // 添加过滤条件
        lqw.like(Strings.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

}
