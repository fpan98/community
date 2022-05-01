$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});



function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId, "postId":postId},
        function(data) {
            console.log(data); // 如果没有登录的话{"msg":"服务器异常！","code":1}
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"postId":$("#postId").val(), "userType":2},  // 只有版主才有这样的权限，将这两个值传给后端
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精华
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"postId":$("#postId").val(), "userType":2},  //只有版主有这样的权限
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"postId":$("#postId").val(), "userType":1}, // 只有管理员有删除的权限
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}