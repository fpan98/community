package com.mininowcoder.community.util;

/**
 * Created by FeiPan on 2022/4/21.
 * 封装分页相关的信息
 */
public class Page {
    // 当前页码
    private int current = 1;
    // 每页显示的上线
    private int limit = 10;
    // 数据总数-用于计算总页数
    private int rows;
    // 查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // 当前页面的起始行
    public int getOffset() {
        return (current - 1) * limit;
    }

    // 获取总的页数
    public int getTotal() {
        return (int) Math.ceil(rows * 1.0 / limit);
    }

    // 获取起始页，显示当前页及其周围的两页
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    // 获取结束页
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
