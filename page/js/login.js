function loading() {
    let xhr = new XMLHttpRequest();
    xhr.open("get", "/file", true);
    xhr.send();
    xhr.onreadystatechange = () => {
        if (xhr.status === 200 && xhr.readyState === 4) {
            dealFileList(JSON.parse(xhr.responseText));
        }
    };
}

nowDir = "";
nowDirName = "dongmingchao";
rootDir = "/Users/dongmingchao";
rootDirName = "dongmingchao";

function dealFileList(rec) {
    for (let i = 0; i < rec.length; i++) {
        inspectFile(rec[i], i);
        let ch = $('<tr id="tr_' + i + '">');
        let swit = $('<div class="switch small">');
        swit.append($('<input type="checkbox" id="inCheck_' + i + '">'));
        swit.append($('<label for="inCheck_' + i + '">'));
        ch.append($('<td>').append(swit));
        ch.append($('<td>').append($('<a id="fileName_' + i + '">')));
        ch.append($('<td id="size_' + i + '">'));
        ch.append($('<td id="lastModeified_' + i + '">'));
        $('#fileList').append(ch);
    }
}

function sortFile(suffix) {
    switch (suffix) {
        case 'png':
        case 'jpg':
        case 'bmp':
        case 'gif':
            return 'Pictures';
        case 'doc':
        case 'docx':
        case 'xls':
        case 'xlsx':
        case 'pdf':
        case 'ppt':
            return 'Documents';
        case 'avi':
        case 'mp4':
        case 'mkv':
            return 'Movies';
        case 'mp3':
        case 'flac':
        case 'wma':
            return 'Music';
        default:
            return 'Others';
    }
}

function inspectFile(path, id) {
    let xhr = new XMLHttpRequest();
    xhr.open("get", "/fileinspect?name=" + path, true);
    xhr.send();
    xhr.onreadystatechange = () => {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let inspect = JSON.parse(xhr.responseText);
            $('#size_' + id).text(inspect.size);
            $('#lastModeified_' + id).text(inspect.lastModified);
            let trName = $('#fileName_' + id);
            if (inspect.name === nowDirName) {
                if (nowDir === "") {
                    trName.text("主文件夹");
                    trName.attr('href', 'javascript:kick(\'' + rootDir + '\',false)');
                } else {
                    trName.text("返回上一层文件夹");
                    trName.attr('href', 'javascript:kick(\'' + path.split('/' + nowDirName)[0] + '\',true)');
                }
                return;
            } else {
                trName.text(inspect.name);
                trName.attr('href', 'javascript:kick(\'' + path + '\',false)');
            }
            let type = inspect.name.split('.');
            let item = $("#tr_" + id);
            item.addClass(sortFile(type[type.length - 1]));
            if (inspect.name.startsWith('.')) {
                item.addClass('Hide');
                item.hide();
            }
            let toolbar = $('<ul class="button-group">');
            toolbar.append($('<li>').append($('<a class="button small" href="' + nowDir + '/' + inspect.name + '" download="' + inspect.name + '">').text('下载')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.open(\'' + nowDir + '/' + inspect.name + '\')">').text('查看')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="deleteFile(\''+path+'\')">').text('删除')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" data-reveal-id="detail_' + id + '">').text('详细信息')));
            createModal(id, inspect);
            if (!inspect.isDirectory) item.append(toolbar);
        }
    }
}

loading();
$(document).ready(function () {
    $(document).foundation();
});

function deleteFile(path) {
    let xhr = new XMLHttpRequest();
    xhr.open("get","delete?path="+path,true);
    xhr.send();
    xhr.onreadystatechange = () => {
        if (xhr.status===200&&xhr.readyState===4){
            alert(xhr.responseText);
            kick($('#upPath').val());
        }
    }
}

function createModal(id, inspect) {
    let r = $('<div id="detail_' + id + '" class="reveal-modal tiny" data-reveal>');
    r.append($('<h3>').text('详细信息'));
    let ta = $('<table>');
    ta.append($('<tr>').append($('<td>').text('文件名')).append($('<td>').text(inspect.name)));
    ta.append($('<tr>').append($('<td>').text('是否是目录')).append($('<td>').text(inspect.isDirectory)));
    r.append(ta);
    $('body').append(r);
}

function kick(item, back) {
    if (!item.split('.')[1]) cd(item, back);
}

$('.side-nav> li').click(function () {
    $(this).addClass('active');
    $(this).siblings('.active').removeClass('active');
    let ta = $('#fileList');
    switch ($(this)[0].innerText) {
        case '全部文件':
            ta.find('tr').show();
            ta.find('tr.Hide').hide();
            break;
        case '图片':
            ta.find('tr.Pictures').show();
            ta.find('tr:not(.Pictures)').hide();
            break;
        case '文档':
            ta.find('tr.Documents').show();
            ta.find('tr:not(.Documents)').hide();
            break;
        case '视频':
            ta.find('tr.Movies').show();
            ta.find('tr:not(.Movies)').hide();
            break;
        case '音乐':
            ta.find('tr.Music').show();
            ta.find('tr:not(.Music)').hide();
            break;
        case '其他':
            ta.find('tr.Others').show();
            ta.find('tr:not(.Others)').hide();
            break;
        case '隐藏文件':
            ta.find('tr.Hide').show();
            ta.find('tr:not(.Hide)').hide();
            break;
    }
});

function cd(directory, toback) {
    let getName = directory.split("/");
    let orgName = nowDirName;
    nowDirName = getName[getName.length - 1];
    if (toback && (nowDir !== "")) nowDir = nowDir.split('/' + orgName)[0];
    else {
        if (nowDirName !== rootDirName)
            nowDir = nowDir + '/' + nowDirName;
    }
    $('#upPath').val('/Users/dongmingchao' + nowDir);
    $('#fileList').empty();
    let stepIn = new XMLHttpRequest();
    stepIn.open("get", "/find?dirName=" + directory, true);
    stepIn.send();
    stepIn.onreadystatechange = () => {
        if (stepIn.status === 200 && stepIn.readyState === 4) {
            dealFileList(JSON.parse(stepIn.responseText));
        }
    }
}

$("#up").click(function () {
    let formData = new FormData();
    formData.append("path",$('#upPath').val());
    formData.append("file", document.getElementById("choseFile").files[0]);
    let xhr = new XMLHttpRequest();
    xhr.open("post","upload",true);
    xhr.send(formData);
    xhr.onreadystatechange = () => {
        if (xhr.status===200&&xhr.readyState===4){
            alert(xhr.responseText);
            kick($('#upPath').val());
        }
    }
});