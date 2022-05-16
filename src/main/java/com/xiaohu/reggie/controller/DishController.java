package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.dto.DishDto;
import com.xiaohu.reggie.entity.Category;
import com.xiaohu.reggie.entity.Dish;
import com.xiaohu.reggie.entity.DishFlavor;
import com.xiaohu.reggie.service.CategoryService;
import com.xiaohu.reggie.service.DishFlavorService;
import com.xiaohu.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> saveDish(@RequestBody DishDto dishDto) {
        log.info("Dish数据:{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页构造对象
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page();
        // 条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();

        lqw.like(name != null, Dish::getName, name);

        lqw.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, lqw);
        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((res) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(res, dishDto);
            Long categoryId = res.getCategoryId();//分类id
            // 根据id查询分类的对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(pageInfo);
    }

    /**
     * 根据id查询菜品信息及口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("菜品的id为:{}", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("dishDto:{}", dishDto);
        dishService.updateDishWithFlavor(dishDto);
        return R.success("保存成功");
        //dishService
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // 查询 起售 状态的菜品 status=1
        lqw.eq(Dish::getStatus, 1);
        lqw.eq(dish.getId() != null, Dish::getCategoryId, dish.getId());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lqw);
        List<DishDto> dishDtoList = dishList.stream().map((res) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(res, dishDto);
            Long categoryId = res.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long dishId = res.getId();//菜品id
            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavorList = dishFlavorService.list(flavorLambdaQueryWrapper);
            dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }

}
