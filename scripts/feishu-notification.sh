#!/usr/bin/env bash

# 判断事件类型
if [ "$GITHUB_EVENT_NAME" = "push" ]; then
  TITLE="GitHub 代码推送 - ${GITHUB_REF_NAME}"
  CONTENT="推送人: ${PUSH_COMMITTER}\n提交信息: ${PUSH_COMMIT_MSG}"
elif [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
  case "$PR_ACTION" in
    opened)
      TITLE="GitHub 新 PR - ${GITHUB_REF_NAME}"
      ;;
    synchronize)
      TITLE="GitHub PR 更新 - ${GITHUB_REF_NAME}"
      ;;
    closed)
      if [ "$PR_MERGED" = "true" ]; then
        TITLE="GitHub PR 已合并 - ${GITHUB_REF_NAME}"
      else
        TITLE="GitHub PR 已关闭 - ${GITHUB_REF_NAME}"
      fi
      ;;
  esac
  CONTENT="发起人: ${PR_USER}\n标题: ${PR_TITLE}"
else
  exit 0
fi

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FEISHU_WEBHOOK_URL}" \
  -H "Content-Type: application/json" \
  -d "{\"msg_type\":\"text\",\"content\":{\"text\":\"${TITLE}\n${CONTENT}\"}}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP ${HTTP_CODE}: ${BODY}"

if [ "$HTTP_CODE" != "200" ]; then
  echo "飞书通知发送失败"
  exit 1
fi
