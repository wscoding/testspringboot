<template>
  <el-button type="danger" @click="setUserInfo('客服',this.father,null)">客服登陆</el-button>
  <el-button type="warning"
             @click="setUserInfo(
               '游客'+randomString(false,5),
             randomRange(1,1000),
             this.father)"
  >随机游客
  </el-button>
  <br>
  {{ userName }} - {{ userId }}
  <el-divider />

  <div class="mt-4">
    <el-input v-model="content">
      <template #prepend>
        消息内容
      </template>
      <template #append>
        <el-button type="success" @click="sendMsg">发送消息</el-button>
      </template>
    </el-input>
  </div>

  <el-divider />

  <div class="demo-type">
    <div v-for="user in fromUserList">
      <el-avatar :class="this.toUserId === user.id ? 'activeAvatar':''" @click="this.toUserId=user.id">{{ user.id }}
      </el-avatar>
    </div>
  </div>

  <el-divider />

  <el-row>
    <el-col>
      <div v-for="msg in msgList[this.toUserId]">
        <div class="msg" v-if="!msg.flag">
          <span style="">
            <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            {{ msg.content }}
          </span>
          <br>
        </div>
        <div class="msg" v-if="msg.flag">
          <span style="float:right;clear: both">
            {{ msg.content }}
            <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          </span>
          <br>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import { ElMessage, ElMessageBox } from "element-plus";

export default {
  name: "Chat",
  data() {
    return {
      father: 1,
      userName: null,
      userId: null,
      toUserId: null, ////当前正在对话的用户
      content: null,  //要发送的消息内容
      msgList: [],
      fromUserList: [],
      socketUrl: "ws://127.0.0.1:8080/webSocket/",
      socket: null
    };
  },
  methods: {
    initWebSocket() {
      this.socket = new WebSocket(this.socketUrl + this.userName + "/" + this.userId);
      if (!this.socket) {
        console.log("您的浏览器不支持websocket协议！");
      }

      this.socket.onopen = this.webSocketOnOpen;
      this.socket.onmessage = this.webSocketOnMessage;
      this.socket.onerror = this.webSocketOnError;
      this.socket.onclose = this.webSocketOnClose;
    },
    // 连接成功后调用
    webSocketOnOpen: function(e) {
      console.log("webSocketOnOpen - 连接成功", e);
    },
    // 发生错误时调用
    webSocketOnError: function(e) {
      console.log("webSocketOnError - 连接错误", e);
    },
    // 给后端发消息时调用
    webSocketSend: function(e) {
      console.log("WebSocketSend - 发送消息", e);
    },
    // 接收后端消息
    webSocketOnMessage: function(e) {
      let data = e.data;
      let msgResult = JSON.parse(data);
      msgResult.flag = false;

      if (msgResult.type && msgResult.type === "MSG") {
        if (this.userId === this.father) {  //如果当前用户是客服 那么则插入到指定用户的数组里
          if (!this.msgList[msgResult.userId]) {
            this.msgList[msgResult.userId] = [];
          }
          this.msgList[msgResult.userId].push(msgResult);
        } else { //当前是用户 则插入到客服的数组里
          if (!this.msgList[this.father]) {
            this.msgList[this.father] = [];
          }
          this.msgList[this.father].push(msgResult);
        }
      } else {
        if (msgResult) {
          if (msgResult.type === "FORCEDLOGINOUT") {
            ElMessageBox.alert(msgResult.content, "退出提示", {
              callback: () => {
                window.location.reload();
              }
            });
          } else if (msgResult.type === "FROMUSER") {
            this.pushFromUserList(msgResult.user);
          }
        }
      }
    },
    // 关闭连接时调用
    webSocketOnClose: function(e) {
      console.log("webSocketOnClose - 关闭连接", e);
    },
    setUserInfo: function(userName, userId, toUserId) {
      if (this.socket && this.socket.readyState === 1) {
        ElMessage.error("您已经登陆，" + this.userName);
        return false;
      }
      this.userId = userId;
      this.userName = userName;
      this.toUserId = toUserId;

      if (!this.userName || !this.userId || (!this.toUserId && this.userId != 1)) {
        ElMessage.error("未登录");
        return false;
      }
      this.initWebSocket();
    },
    sendMsg: function() {
      if (!this.socket || this.socket.readyState !== 1) {
        ElMessage.error("连接服务器失败！");
        return false;
      }
      if (!this.content) {
        ElMessage.error("请输入要发送的消息！");
        return false;
      }
      if (!this.toUserId) {
        ElMessage.error("请选择要回复的用户！");
        return false;
      }
      //封装发送消息对象
      let msg = {
        content: this.content,
        toUserId: this.toUserId,
        flag: true
      };
      this.socket.send(JSON.stringify(msg));

      if (this.userId === this.father) {  //如果当前用户是客服 那么则插入到指定用户的数组里
        if (!this.msgList[msg.toUserId]) {
          this.msgList[msg.toUserId] = [];
        }
        this.msgList[msg.toUserId].push(msg);
      } else { //当前是用户 则插入到客服的数组里
        if (!this.msgList[this.father]) {
          this.msgList[this.father] = [];
        }
        this.msgList[this.father].push(msg);
      }
    },
    pushFromUserList: function(user) {
      let id = user.id;
      let flag = false;
      if (id) {
        if (this.fromUserList.length === 0) {
          this.fromUserList.push(user);
        } else {
          this.fromUserList.forEach((val) => {
            if (id === val) {
              flag = true;
            }
          });
          if (!flag) {
            this.fromUserList.push(user);
          }
        }
      }
    },
    randomString: function(randomLen, min, max) {
      let str = "",
        range = min,
        arr = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
          "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
          "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
          "w", "x", "y", "z", "A", "B", "C", "D", "E", "F",
          "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
          "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];
      // 随机产生
      if (randomLen) {
        range = Math.round(Math.random() * (max - min)) + min;
      }
      for (var i = 0; i < range; i++) {
        let pos = Math.round(Math.random() * (arr.length - 1));
        str += arr[pos];
      }
      return str;
    },
    randomRange: function(min, max) { // min最小值，max最大值
      return Math.floor(Math.random() * (max - min)) + min;
    }
  }
};
</script>

<style>
.chat-me {
  width: 100%;
  line-height: 35px;
  color: #2F76D3;
  text-align: right;
  font-weight: bold;
  padding-right: 45px;
}

.msg:nth-child(2n-1) {
  clear: both
}

.demo-type {
  display: flex;
}

.demo-type > div {
  flex: 1;
  text-align: center;
}

.demo-type > div:not(:last-child) {
  border-right: 1px solid var(--el-border-color);
}

span.el-avatar.el-avatar--circle.activeAvatar {
  --el-avatar-bg-color: #f56c6c
}
</style>