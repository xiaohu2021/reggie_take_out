package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.entity.Category;
import com.xiaohu.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    public R<Page> getAllCategory(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);
        // 条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        //进行分页查询
        categoryService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> deleteRecord(Long ids) {
        log.info("这个id为：{}", ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 根据id来修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息:{}", category);
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        // 参数可以传 int type
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // lqw.eq(Category::getType, category.getType());
        //添加条件
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);
        return R.success(list);
    }

}
