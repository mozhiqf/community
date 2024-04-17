document.addEventListener('DOMContentLoaded', function () {
    console.log('Signature from window:', window.signature);
    const uploadForm = document.querySelector('form[action="#"]'); // 确保这是上传头像的表单
    const fileInput = document.querySelector('#head-image');
    uploadForm.addEventListener('submit', function (event) {
        event.preventDefault(); // 阻止表单默认提交
        const file = fileInput.files[0];
        const filename = file.name;

        // 从全局变量获取OSS上传凭证
        const data = JSON.parse(window.signature);

        const formData = new FormData();
        formData.append('key', data.dir + data.fileName);
        formData.append('name', data.fileName);
        formData.append('OSSAccessKeyId', data.ossAccessKeyId);
        formData.append('policy', data.policy);
        formData.append('Signature', data.signature);
        formData.append('file', file); // 文件本身
        console.log('data.dir:', data.dir);
        console.log('data.fileName', data.fileName);
        console.log('data.ossAccessKeyId:', data.ossAccessKeyId);
        console.log('data.policy:', data.policy);
        console.log('data.signature:', data.signature);
        console.log('data.host:', data.host);
        // 使用fetch API上传文件到OSS
        fetch(data.host, {
            method: 'POST',
            body: formData,
        }).then(response => {
            if (response.ok) {
                console.log('上传成功');
                //
                //https://mzqf-forum-header.oss-cn-beijing.aliyuncs.com/header/test.txt
                let fullFilePath = `${data.host}/${data.dir}${data.fileName}`;
                // 发起异步请求到后端更新头像URL
                return fetch(CONTEXT_PATH + '/user/header/url', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({fileName: fullFilePath}),
                });
            } else {
                throw new Error('上传到OSS失败');
            }
        }).then(response => {
            if (response.ok) {
                console.log('头像URL更新成功');
                alert('头像上传并更新成功');
            } else {
                throw new Error('更新头像URL失败');
            }
        }).catch(error => {
            console.error('Error:', error);
            alert('过程中发生错误，请检查控制台日志');
        });
    });
});
