package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.CustomException;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate redisTemplate;

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

        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        // 精确清理 清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("保存成功");
        //dishService
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
//        // 动态构造key
//        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
//        // 从redis 中获取缓存数据
//        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
//        // 如果存在 直接返回 无需查询数据库
//        if (dishDtoList != null) {
//            return R.success(dishDtoList);
//        }
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        // 查询 起售 状态的菜品 status=1
        lqw.eq(Dish::getStatus, 1);
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lqw);
        dishDtoList = dishList.stream().map((res) -> {
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
        // 如果不存在 需要查询数据库 将查询到的菜品数据缓存
        // redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    /**
     * 批量起售 停售 或单个起售 停售功能开发。
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status, Long[] ids) {
        log.info("status: {},ids:{}", status, ids);
        if (ids == null || ids.length == 0) {
            throw new CustomException("参数有误");
        }
        LambdaUpdateWrapper<Dish> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Dish::getStatus, status).in(Dish::getId, ids);
        dishService.update(lambdaUpdateWrapper);
        return R.success(status == 1 ? "已起售" : "已停售");
    }


    @DeleteMapping
    public R<String> deleteDish(Long[] ids) {
        log.info("ids:{}", ids);
        if (ids == null || ids.length == 0) {
            throw new CustomException("未选择该删除的选项");
        }
        List<Dish> dishList = dishService.listByIds(Arrays.asList(ids));
        for (Dish dish : dishList) {
            if (dish.getStatus() == 1) {
                throw new CustomException(dish.getName() + "商品为起售状态不能删除");
            }
        }
        LambdaUpdateWrapper<Dish> dishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        dishLambdaUpdateWrapper.set(Dish::getIsDeleted, 1).in(Dish::getId, ids);
        dishService.update(dishLambdaUpdateWrapper);
        return R.success("删除成功");
    }

}
