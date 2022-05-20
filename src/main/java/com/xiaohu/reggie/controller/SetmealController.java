package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.dto.SetmealDto;
import com.xiaohu.reggie.entity.Category;
import com.xiaohu.reggie.entity.Setmeal;
import com.xiaohu.reggie.entity.SetmealDish;
import com.xiaohu.reggie.service.CategoryService;
import com.xiaohu.reggie.service.SetmealDishService;
import com.xiaohu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;


    @PostMapping
    @CacheEvict(value="setmealCache",allEntries = true)
    public R<String> saveSetmeal(@RequestBody SetmealDto setmealDto) {
        log.info("套餐:{}", setmealDto);
        setmealService.saveSetmealWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page();
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
//        LambdaQueryWrapper<SetmealDto> lqw = new LambdaQueryWrapper<>();
//        lqw.like(name != null, SetmealDto::getCategoryName, name);
        lqw.like(name != null, Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, lqw);
        //对象复制
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list =
                records.stream().map((res) -> {
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(res, setmealDto);
                    // 分类id
                    Long categoryId = res.getCategoryId();
                    // 根据分类id查询分类对象
                    Category category = categoryService.getById(categoryId);
                    if (category != null) {
                        String categoryName = category.getName();
                        setmealDto.setCategoryName(categoryName);
                    }
                    return setmealDto;
                }).collect(Collectors.toList());
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @CacheEvict(value="setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    @Cacheable(value="setmealCache",key="#setmeal.categoryId + '_'+ #setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
       lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(lqw);
        return R.success(setmealList);
    }
}
