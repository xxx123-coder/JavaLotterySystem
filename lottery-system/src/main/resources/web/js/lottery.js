/**
 * 彩票系统JavaScript文件
 * 包含前端交互逻辑和工具函数
 */

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    initLotterySystem();
});

/**
 * 初始化彩票系统
 */
function initLotterySystem() {
    console.log('彩票系统初始化...');

    // 初始化事件监听器
    initEventListeners();

    // 检查登录状态
    checkLoginStatus();

    // 更新页面时间
    updateDateTime();
}

/**
 * 初始化事件监听器
 */
function initEventListeners() {
    // 彩票号码输入框的验证
    const numberInputs = document.querySelectorAll('.number-input');
    numberInputs.forEach(input => {
        input.addEventListener('input', validateNumberInput);
    });

    // 购买彩票表单提交
    const buyForm = document.getElementById('buy-ticket-form');
    if (buyForm) {
        buyForm.addEventListener('submit', handleBuyTicket);
    }

    // 抽奖按钮
    const drawButton = document.getElementById('draw-button');
    if (drawButton) {
        drawButton.addEventListener('click', handleDraw);
    }

    // 注册表单
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // 登录表单
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
}

/**
 * 检查用户登录状态
 */
function checkLoginStatus() {
    // 从localStorage或cookie中检查登录状态
    const token = localStorage.getItem('auth_token');
    const userInfo = localStorage.getItem('user_info');

    if (token && userInfo) {
        try {
            const user = JSON.parse(userInfo);
            updateUserInterface(user);
        } catch (e) {
            console.error('解析用户信息失败:', e);
            clearAuthData();
        }
    }
}

/**
 * 更新用户界面
 */
function updateUserInterface(user) {
    // 更新导航栏显示用户信息
    const userMenu = document.getElementById('user-menu');
    if (userMenu) {
        userMenu.innerHTML = `
            <div class="user-info">
                <span>欢迎, ${user.username}</span>
                <span class="balance">余额: ¥${user.balance.toFixed(2)}</span>
                <button onclick="logout()" class="btn-logout">退出</button>
            </div>
        `;
    }

    // 显示用户特定的功能
    const guestOnly = document.querySelectorAll('.guest-only');
    guestOnly.forEach(element => {
        element.style.display = 'none';
    });

    const userOnly = document.querySelectorAll('.user-only');
    userOnly.forEach(element => {
        element.style.display = 'block';
    });
}

/**
 * 验证彩票号码输入
 */
function validateNumberInput(event) {
    const input = event.target;
    const value = parseInt(input.value);

    // 清除非数字字符
    input.value = input.value.replace(/\D/g, '');

    // 验证范围（1-36）
    if (input.value !== '') {
        const num = parseInt(input.value);
        if (num < 1 || num > 36) {
            input.classList.add('error');
            showError('号码必须在1-36之间');
            return;
        }
    }

    input.classList.remove('error');
}

/**
 * 处理购买彩票
 */
async function handleBuyTicket(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    // 验证输入
    if (!validateTicketData(data)) {
        return;
    }

    try {
        showLoading();

        const response = await fetch('/api/buy-ticket', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            showSuccess(result.message);

            // 更新用户余额
            if (result.userBalance !== undefined) {
                updateUserBalance(result.userBalance);
            }

            // 重置表单
            event.target.reset();
        } else {
            showError(result.message);
        }
    } catch (error) {
        console.error('购买彩票失败:', error);
        showError('网络错误，请稍后重试');
    } finally {
        hideLoading();
    }
}

/**
 * 处理抽奖
 */
async function handleDraw() {
    try {
        showLoading('正在抽奖中...');

        // 显示动画效果
        startDrawAnimation();

        const response = await fetch('/api/draw', {
            method: 'POST'
        });

        const result = await response.json();

        if (result.success) {
            // 停止动画，显示结果
            stopDrawAnimation();
            displayDrawResult(result);

            // 播放中奖音效（如果有的话）
            playWinSound();
        } else {
            showError(result.message);
        }
    } catch (error) {
        console.error('抽奖失败:', error);
        showError('抽奖失败，请稍后重试');
    } finally {
        hideLoading();
    }
}

/**
 * 显示抽奖动画
 */
function startDrawAnimation() {
    const balls = document.querySelectorAll('.lottery-ball');
    balls.forEach(ball => {
        ball.classList.add('rolling');
        // 设置随机数字
        ball.textContent = Math.floor(Math.random() * 36) + 1;
    });

    // 显示动画容器
    const animationContainer = document.getElementById('draw-animation');
    if (animationContainer) {
        animationContainer.style.display = 'block';
    }
}

/**
 * 停止抽奖动画并显示结果
 */
function stopDrawAnimation() {
    const balls = document.querySelectorAll('.lottery-ball');
    balls.forEach(ball => {
        ball.classList.remove('rolling');
    });
}

/**
 * 显示抽奖结果
 */
function displayDrawResult(result) {
    const resultContainer = document.getElementById('draw-result');
    if (resultContainer) {
        let html = `
            <div class="draw-result-content">
                <h3>抽奖结果</h3>
                <div class="winning-numbers">
                    ${result.winningNumbers.split(',').map(num =>
            `<span class="winning-ball">${num}</span>`
        ).join('')}
                </div>
        `;

        if (result.winners && result.winners.length > 0) {
            html += `
                <div class="winners-list">
                    <h4>中奖者</h4>
                    <ul>
                        ${result.winners.map(winner =>
                `<li>${winner.username} - ${winner.prizeLevel}</li>`
            ).join('')}
                    </ul>
                </div>
            `;
        } else {
            html += `<p class="no-winner">本期无人中大奖</p>`;
        }

        html += `</div>`;
        resultContainer.innerHTML = html;
        resultContainer.style.display = 'block';
    }
}

/**
 * 处理用户注册
 */
async function handleRegister(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    // 验证表单
    if (!validateRegisterData(data)) {
        return;
    }

    try {
        showLoading('正在注册...');

        const response = await fetch('/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            showSuccess('注册成功！正在跳转到登录页面...');

            // 保存用户信息
            if (result.user) {
                localStorage.setItem('auth_token', result.token || '');
                localStorage.setItem('user_info', JSON.stringify(result.user));
                updateUserInterface(result.user);
            }

            // 3秒后跳转到登录页面
            setTimeout(() => {
                window.location.href = '/login';
            }, 3000);
        } else {
            showError(result.message);
        }
    } catch (error) {
        console.error('注册失败:', error);
        showError('注册失败，请稍后重试');
    } finally {
        hideLoading();
    }
}

/**
 * 处理用户登录
 */
async function handleLogin(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    try {
        showLoading('正在登录...');

        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            // 保存认证信息
            localStorage.setItem('auth_token', result.token || '');
            localStorage.setItem('user_info', JSON.stringify(result.user));

            showSuccess('登录成功！');
            updateUserInterface(result.user);

            // 跳转到主页面
            setTimeout(() => {
                window.location.href = '/main';
            }, 1000);
        } else {
            showError(result.message);
        }
    } catch (error) {
        console.error('登录失败:', error);
        showError('登录失败，请检查网络连接');
    } finally {
        hideLoading();
    }
}

/**
 * 用户退出
 */
function logout() {
    // 清除认证信息
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');

    // 刷新页面
    window.location.reload();
}

/**
 * 验证彩票数据
 */
function validateTicketData(data) {
    // 验证号码
    if (data.ticketType === 'manual') {
        if (!data.numbers || data.numbers.trim() === '') {
            showError('请输入彩票号码');
            return false;
        }

        const numbers = data.numbers.split(',').map(n => parseInt(n.trim()));

        // 验证数量
        if (numbers.length !== 7) {
            showError('必须输入7个号码');
            return false;
        }

        // 验证范围
        for (const num of numbers) {
            if (isNaN(num) || num < 1 || num > 36) {
                showError('号码必须在1-36之间');
                return false;
            }
        }

        // 验证重复
        const uniqueNumbers = new Set(numbers);
        if (uniqueNumbers.size !== 7) {
            showError('号码不能重复');
            return false;
        }
    }

    // 验证注数
    const betCount = parseInt(data.betCount);
    if (isNaN(betCount) || betCount < 1 || betCount > 100) {
        showError('注数必须在1-100之间');
        return false;
    }

    return true;
}

/**
 * 验证注册数据
 */
function validateRegisterData(data) {
    // 验证用户名
    if (!data.username || data.username.length < 3 || data.username.length > 20) {
        showError('用户名长度必须在3-20个字符之间');
        return false;
    }

    // 验证密码
    if (!data.password || data.password.length < 6) {
        showError('密码长度至少6个字符');
        return false;
    }

    // 验证手机号
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!data.phone || !phoneRegex.test(data.phone)) {
        showError('请输入有效的手机号码');
        return false;
    }

    return true;
}

/**
 * 显示加载动画
 */
function showLoading(message = '加载中...') {
    // 创建或显示加载遮罩
    let loadingOverlay = document.getElementById('loading-overlay');

    if (!loadingOverlay) {
        loadingOverlay = document.createElement('div');
        loadingOverlay.id = 'loading-overlay';
        loadingOverlay.className = 'loading-overlay';
        loadingOverlay.innerHTML = `
            <div class="loading-spinner"></div>
            <div class="loading-text">${message}</div>
        `;
        document.body.appendChild(loadingOverlay);

        // 添加CSS样式
        const style = document.createElement('style');
        style.textContent = `
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.7);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                z-index: 9999;
                color: white;
            }
            .loading-spinner {
                width: 50px;
                height: 50px;
                border: 5px solid #f3f3f3;
                border-top: 5px solid #667eea;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin-bottom: 20px;
            }
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }

    loadingOverlay.style.display = 'flex';
}

/**
 * 隐藏加载动画
 */
function hideLoading() {
    const loadingOverlay = document.getElementById('loading-overlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

/**
 * 显示成功消息
 */
function showSuccess(message) {
    showMessage(message, 'success');
}

/**
 * 显示错误消息
 */
function showError(message) {
    showMessage(message, 'error');
}

/**
 * 显示消息
 */
function showMessage(message, type) {
    // 移除现有的消息
    const existingMessage = document.querySelector('.message-overlay');
    if (existingMessage) {
        existingMessage.remove();
    }

    // 创建消息元素
    const messageOverlay = document.createElement('div');
    messageOverlay.className = `message-overlay ${type}`;
    messageOverlay.textContent = message;

    // 添加CSS样式
    const style = document.createElement('style');
    style.textContent = `
        .message-overlay {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 25px;
            border-radius: 5px;
            color: white;
            font-weight: bold;
            z-index: 10000;
            animation: slideIn 0.3s ease-out;
            max-width: 300px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
        }
        .message-overlay.success {
            background: #48bb78;
        }
        .message-overlay.error {
            background: #f56565;
        }
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(messageOverlay);

    // 3秒后自动消失
    setTimeout(() => {
        messageOverlay.style.animation = 'slideOut 0.3s ease-out forwards';

        // 添加淡出动画
        const fadeStyle = document.createElement('style');
        fadeStyle.textContent = `
            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(fadeStyle);

        setTimeout(() => {
            messageOverlay.remove();
            fadeStyle.remove();
        }, 300);
    }, 3000);
}

/**
 * 更新用户余额显示
 */
function updateUserBalance(balance) {
    const balanceElements = document.querySelectorAll('.user-balance');
    balanceElements.forEach(element => {
        element.textContent = `¥${balance.toFixed(2)}`;
    });
}

/**
 * 更新日期时间
 */
function updateDateTime() {
    const now = new Date();
    const dateTimeElements = document.querySelectorAll('.current-datetime');

    dateTimeElements.forEach(element => {
        element.textContent = now.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    });

    // 每秒更新一次
    setTimeout(updateDateTime, 1000);
}

/**
 * 清空认证数据
 */
function clearAuthData() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
}

/**
 * 播放中奖音效
 */
function playWinSound() {
    // 如果有音频文件，可以在这里播放
    // 例如：new Audio('/sounds/win.mp3').play();
    console.log('中奖音效播放');
}

/**
 * 生成随机彩票号码
 */
function generateRandomNumbers() {
    const numbers = [];
    while (numbers.length < 7) {
        const num = Math.floor(Math.random() * 36) + 1;
        if (!numbers.includes(num)) {
            numbers.push(num);
        }
    }
    numbers.sort((a, b) => a - b);
    return numbers.join(',');
}

/**
 * 将随机号码填入表单
 */
function fillRandomNumbers() {
    const numbersInput = document.getElementById('numbers-input');
    if (numbersInput) {
        numbersInput.value = generateRandomNumbers();
    }
}

// 导出全局函数
window.logout = logout;
window.fillRandomNumbers = fillRandomNumbers;