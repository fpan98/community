package com.mininowcoder.community.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mininowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.ObjenesisHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/28.
 */

@Service
public class ElasticsearchService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    // 向elasticsearch中提交发布的帖子
    public void saveDiscussPost(DiscussPost discussPost) {
        IndexRequest indexRequest = new IndexRequest("discusspost");
        try {
            indexRequest.id(String.valueOf(discussPost.getId()))
                    .source(new ObjectMapper().writeValueAsString(discussPost), XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("向elasticsearch中添加文档失败:"+e.getMessage());
        }
    }

    // 删除elasticsearch中的文档
    public void deleteDiscusspost(int id){
        try {
            restHighLevelClient.delete(new DeleteRequest("discusspost", String.valueOf(id)), RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("向elasticsearch中删除文档失败:"+e.getMessage());
        }
    }

    // 搜索
    public Map<String, Object> searchDiscussPost(String keyword, int offset, int limit){
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false)
                .field("title")
                .field("content")
                .preTags("<em>")
                .postTags("</em>");
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "title","content"))
                .sort("type", SortOrder.DESC)
                .sort("score", SortOrder.DESC)
                .sort("createTime", SortOrder.DESC)
                .from(offset)
                .size(limit)
                .highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("在elasticsearch中查询失败:"+e.getMessage());
        }
        int totalHits = (int) search.getHits().getTotalHits().value;
        SearchHit[] hits = search.getHits().getHits();
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            DiscussPost post = null;
            try {
                post = new ObjectMapper().readValue(hit.getSourceAsString(), DiscussPost.class);
            } catch (JsonProcessingException e) {
                logger.error("elasticsearch中的json数据解析成DiscussPost对象失败:"+e.getMessage());
            }
            if(post!=null){
                // 判断是否包含高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(highlightFields.containsKey("title")){
                    post.setTitle(highlightFields.get("title").fragments()[0].toString());
                }
                if(highlightFields.containsKey("content")){
                    post.setContent(highlightFields.get("content").fragments()[0].toString());
                }
                list.add(post);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("discussPosts", list);
        map.put("totalHits", totalHits);
        return map;
    }

}
