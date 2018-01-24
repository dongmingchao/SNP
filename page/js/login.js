function loading() {
    let xhr = new XMLHttpRequest();
    xhr.open("get", "/file", true);
    xhr.send();
    xhr.onreadystatechange = () => {
        if (xhr.status === 200 && xhr.readyState === 4) {
            let rec = JSON.parse(xhr.responseText);
            for (let i = 0; i < rec.length; i++) {
                inspectFile(rec[i],i);
                let each = rec[i].split('/');
                if (!each[1]) each[1] = "..";
                let ch = $('<tr id="tr_'+i+'">');
                let swit = $('<div class="switch small">');
                swit.append($('<input type="checkbox" id="inCheck_'+i+'">'));
                swit.append($('<label for="inCheck_'+i+'">'));
                ch.append($('<td>').append(swit));
                ch.append($('<td>').append($('<a href="#" onclick=kick(\'' + rec[i] + '\')>').text(each[1])));
                ch.append($('<td id="size_'+i+'">'));
                ch.append($('<td id="lastModeified_'+i+'">'));
                $('#fileList').append(ch);
            }
        }
    };
}

function inspectFile(path,id) {
    let xhr = new XMLHttpRequest();
    xhr.open("get","/fileinspect?name="+path,true);
    xhr.send();
    xhr.onreadystatechange = () => {
        if (xhr.readyState===4&&xhr.status===200){
            let inspect = JSON.parse(xhr.responseText);
            $('#size_'+id).text(inspect.size);
            $('#lastModeified_'+id).text(inspect.lastModified);
            let toolbar = $('<ul class="button-group">');
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+path+'">').text('下载')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+path+'">').text('查看')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+path+'">').text('复制')));
            toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+path+'">').text('详细信息')));
            console.log(inspect);
            if (!inspect.isDirectory) $("#tr_"+id).append(toolbar);
        }
    }
}

loading();

function kick(item) {
    console.log(item);
    if (!item.split('.')[1]) cd(item);
}

function cd(directory) {
    $('#fileList').empty();
    let stepIn = new XMLHttpRequest();
    stepIn.open("get", "/find?dirName=" + directory, true);
    stepIn.send();
    stepIn.onreadystatechange = () => {
        if (stepIn.status === 200 && stepIn.readyState === 4) {
            let rec = JSON.parse(stepIn.responseText);
            for (let i = 0; i < rec.length; i++) {
                let each = rec[i].split(directory);
                if (!each[1]) each[1] = "..";
                else each[1] = each[1].substr(1);
                let ch = $('<tr>');
                let swit = $('<div class="switch small">');
                swit.append($('<input type="checkbox" id="'+i+'">'));
                swit.append($('<label for="'+i+'">'));
                ch.append($('<td>').append(swit));
                ch.append($('<td>').append($('<a href="#" onclick=kick(\'' + rec[i] + '\')>').text(each[1])));
                ch.append($('<td>').text('10kb'));
                ch.append($('<td>').text('2018.1.23'));
                let toolbar = $('<ul class="button-group">');
                toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+rec[i]+'">').text('下载')));
                toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+rec[i]+'">').text('查看')));
                toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+rec[i]+'">').text('复制')));
                toolbar.append($('<li>').append($('<button type="button" class="button small" onclick="window.location.href='+rec[i]+'">').text('详细信息')));
                ch.append(toolbar);
                $('#fileList').append(ch);
            }
        }
    }
}