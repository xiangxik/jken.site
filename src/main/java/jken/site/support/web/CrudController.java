/*
 * Copyright (c) 2019.
 * @Link: http://jken.site
 * @Author: ken kong
 * @LastModified: 2019-12-20T22:23:11.895+08:00
 *
 */

package jken.site.support.web;

import com.querydsl.core.types.Predicate;
import jken.site.support.data.jpa.Entity;
import jken.site.support.service.CrudService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Arrays;

public abstract class CrudController<T extends Entity<I>, I extends Serializable> extends BaseController implements InitializingBean {

    @Autowired
    private CrudService<T, I> service;

    private String viewDir;

    @Override
    public void afterPropertiesSet() {
        RequestMapping requestMapping = getClass().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            setViewDir(requestMapping.value()[0]);
        }
    }

    /**
     * 显示列表页面
     *
     * @param model
     * @return
     */
    @GetMapping(produces = "text/html")
    public String showList(Model model) {
        onShowList(model);
        return getViewDir() + "/list";
    }

    /**
     * 显示添加页面
     *
     * @param model
     * @return
     */
    @GetMapping(value = "/add", produces = "text/html")
    public String showDetailAdd(Model model) {
        T entity = getService().createNew();
        return showDetailEdit(entity, model);
    }

    /**
     * 显示编辑页面
     *
     * @param entity
     * @param model
     * @return
     */
    @GetMapping(value = "/{id}", produces = "text/html")
    public String showDetailEdit(@PathVariable("id") T entity, Model model) {
        model.addAttribute("entity", entity);
        onShowEdit(entity, model);
        return getViewDir() + "/edit";
    }

    //=====================================================
    //   以下是REST操作
    //=====================================================

    /**
     * 获取列表数据
     *
     * @param predicate
     * @param pageable
     * @return
     */
    @GetMapping(produces = "application/json")
    @ResponseBody
    public abstract Page<T> list(Predicate predicate, Pageable pageable);

    /**
     * 添加实体
     *
     * @param entity
     * @param bindingResult
     */
    @PostMapping
    @ResponseBody
    public void create(@ModelAttribute @Valid T entity, BindingResult bindingResult) {
        update(entity, bindingResult);
    }

    /**
     * 更新实体
     *
     * @param entity
     * @param bindingResult
     */
    @PutMapping("/{id}")
    @ResponseBody
    public void update(@ModelAttribute("id") @Valid T entity, BindingResult bindingResult) {
        onValidate(entity, bindingResult);
        if (bindingResult.hasErrors()) {
            //
        }

        onBeforeSave(entity);
        getService().save(entity);
        onAfterSave(entity);
    }

    /**
     * 删除实体
     *
     * @param entity
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") T entity) {
        if (onBeforeDelete(entity)) {
            getService().delete(entity);
        }
    }

    /**
     * 批量删除实体
     *
     * @param entities
     */
    @DeleteMapping
    @ResponseBody
    public void batchDelete(@RequestParam(value = "ids[]") T[] entities) {
        if (entities != null) {
            getService().deleteInBatch(Arrays.asList(entities));
        }
    }

    protected Page<T> doInternalPage(Predicate predicate, Pageable pageable) {
        return getService().findAll(predicate, pageable);
    }

    protected void onShowList(Model model) {
    }

    protected void onShowEdit(T entity, Model model) {
    }

    protected void onValidate(T entity, BindingResult bindingResult) {
    }

    protected void onBeforeSave(T entity) {
    }

    protected void onAfterSave(T entity) {
    }

    protected boolean onBeforeDelete(T entity) {
        return true;
    }

    public CrudService<T, I> getService() {
        return service;
    }

    public String getViewDir() {
        return viewDir;
    }

    public void setViewDir(String viewDir) {
        this.viewDir = viewDir;
    }
}
