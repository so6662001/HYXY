import request from '../utils/request'
import type { Result, RiskSummaryVO, RiskDetailVO } from '../types/credit'

export function getRiskSummary(enterpriseId: number) {
  return request.get<Result<RiskSummaryVO>>(`/risk/summary/${enterpriseId}`)
}

export function getRiskDetail(enterpriseId: number) {
  return request.get<Result<RiskDetailVO>>(`/risk/detail/${enterpriseId}`)
}

export function refreshRiskInfo(enterpriseId: number) {
  return request.post(`/risk/refresh/${enterpriseId}`)
}
