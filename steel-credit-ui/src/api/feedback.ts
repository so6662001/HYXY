import request from '../utils/request'
import type { Result, CooperationFeedback, FeedbackSubmitRequest } from '../types/credit'

export function submitFeedback(evaluatorEnterpriseId: number, data: FeedbackSubmitRequest) {
  return request.post<Result<void>>(`/feedback/submit/${evaluatorEnterpriseId}`, data)
}

export function getFeedbackList(targetEnterpriseId: number, evaluatorRole: string) {
  return request.get<Result<CooperationFeedback[]>>(`/feedback/list/${targetEnterpriseId}`, {
    params: { evaluatorRole }
  })
}

export function canEvaluate(evaluatorId: number, targetId: number) {
  return request.get<Result<boolean>>('/feedback/can-evaluate', {
    params: { evaluatorId, targetId }
  })
}
