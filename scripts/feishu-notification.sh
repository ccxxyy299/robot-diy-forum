#!/usr/bin/env bash
set -euo pipefail

# 判断事件类型
if [ "$GITHUB_EVENT_NAME" = "push" ]; then
  TITLE="📦 代码推送 - ${GITHUB_REF_NAME}"
  CONTENT="推送人：${PUSH_COMMITTER}\n提交信息：${PUSH_COMMIT_MSG}"
elif [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
  case "$PR_ACTION" in
    opened)
      TITLE="🔔 新 PR - ${GITHUB_REF_NAME}"
      ;;
    synchronize)
      TITLE="🔄 PR 更新 - ${GITHUB_REF_NAME}"
      ;;
    closed)
      if [ "$PR_MERGED" = "true" ]; then
        TITLE="✅ PR 已合并 - ${GITHUB_REF_NAME}"
      else
        TITLE="❌ PR 已关闭 - ${GITHUB_REF_NAME}"
      fi
      ;;
  esac
  CONTENT="发起人：${PR_USER}\n标题：${PR_TITLE}"
else
  exit 0
fi

PAYLOAD=$(cat <<EOF
{
  "msg_type": "text",
  "content": {
    "text": "${TITLE}\n${CONTENT}"
  }
}
EOF
)

curl -s -X POST "${FEISHU_WEBHOOK_URL}" \
  -H "Content-Type: application/json" \
  -d "${PAYLOAD}"
