package com.xiaojinzi.controller;

import com.xiaojinzi.form.ManForm;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 金全 wrj008
 * @version 1.0.0 2018/1/17.
 * @description es接口编写
 */
@RestController
@RequestMapping("/es")
public class EsApiController {

    @Autowired
    private TransportClient client;

    /**
     * 查询
     * @param id
     * @return
     */
    @GetMapping("/get")
    public ResponseEntity get(@RequestParam("id")String id){
        if(id.isEmpty()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        GetResponse result = this.client.prepareGet("people","man",id).get();
        if(result.getSource().isEmpty()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getSource(),HttpStatus.OK);
    }

    /**
     * 添加文档
     * @param manForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity add(@Valid ManForm manForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            /** json 封装 .*/
            XContentBuilder content = XContentFactory.jsonBuilder().startObject().
                    field("name", manForm.getName()).
                    field("age", manForm.getAge()).
                    field("date", manForm.getDate().getTime()).
                    field("countary", manForm.getCountary()).
                    endObject();
            /** 添加文档 .*/
            IndexResponse result = client.prepareIndex("people","man").setSource(content).get();
            return new ResponseEntity(result.getId(),HttpStatus.OK);
        }catch (IOException e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam("id")String id){
        DeleteResponse result = client.prepareDelete("people","man",id).get();
        return new ResponseEntity(result.getResult().toString(),HttpStatus.OK);
    }

    /**
     * 更新操作
     * @param manForm
     * @param bindingResult
     * @return
     */
    @PutMapping("/update")
    public ResponseEntity update(@Valid ManForm manForm,BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        /** 构建更新 .*/
        UpdateRequest request = new UpdateRequest("people","man",manForm.getId());
        try {
            /** json .*/
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            if(manForm.getName()!= null){
                builder.field("name",manForm.getName());
            }
            if(manForm.getAge()>0){
                builder.field("age",manForm.getAge());
            }
            if(manForm.getCountary()!=null){
                builder.field("countary",manForm.getCountary());
            }
            if(manForm.getDate()!=null){
                builder.field("date",manForm.getDate().getTime());
            }
            builder.endObject();
            request.doc(builder);
        }catch (IOException e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
       try {
            /** 更新操作 .*/
           UpdateResponse result = client.update(request).get();
           return new ResponseEntity(result.getResult().toString(),HttpStatus.OK);
       }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    /**
     * 符合查询
     * @param name
     * @param countary
     * @param lte
     * @param gte
     * @return
     */
    @PostMapping("/query")
    public ResponseEntity query(@RequestParam(name="name",required = false)String name,
                                @RequestParam(name = "countary",required = false)String countary,
                                @RequestParam(name = "age_lte",required = false)Integer lte,
                                @RequestParam(name = "age_gte",defaultValue = "0")int gte){
        /** bool查询构建 .*/
        BoolQueryBuilder boolQuery =  QueryBuilders.boolQuery();
        if(name!=null){
            /**　条件匹配 .*/
            boolQuery.must(QueryBuilders.matchQuery("name",name));
        }
        if(countary!=null){
            boolQuery.must(QueryBuilders.matchQuery("countary",countary));
        }
        /** 范围构建 .*/
        RangeQueryBuilder range = QueryBuilders.rangeQuery("age").from(gte);
        if(lte!= null && lte>0 ){
            range.to(lte);
        }
        /** filter条件添加 .*/
        boolQuery.filter(range);

        /**　搜索条件构建 .*/
        SearchRequestBuilder builder = client.prepareSearch("people").
                setTypes("man").
                setSearchType(SearchType.DFS_QUERY_THEN_FETCH).
                setQuery(boolQuery).
                setFrom(0).
                setSize(10);
        System.out.println(builder);

        /** 结果返回 .*/
        SearchResponse response = builder.get();

        /** 遍历hits .*/
        List<Map<String,Object>> result = new ArrayList<>();
        for(SearchHit hit:response.getHits()){
            result.add(hit.getSource());
        }

        /** 结果返回 .*/
        return new ResponseEntity(result,HttpStatus.OK);
    }
}
