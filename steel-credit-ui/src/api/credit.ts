import request from '../utils/request'
import type { Result, CreditCardVO, CreditDetailVO, CreditBadgeVO } from '../types/credit'

export function getCreditCard(enterpriseId: number) {
  return request.get<Result<CreditCardVO>>(`/credit/card/${enterpriseId}`)
}

export function getBuyerCreditDetail(enterpriseId: number) {
  return request.get<Result<CreditDetailVO>>(`/credit/buyer/detail/${enterpriseId}`)
}

export function getSellerCreditDetail(enterpriseId: number) {
  return request.get<Result<CreditDetailVO>>(`/credit/seller/detail/${enterpriseId}`)
}

export function getCreditBadges(enterpriseIds: number[]) {
  return request.post<Result<CreditBadgeVO[]>>('/credit/badges', enterpriseIds)
}

export function calculateCreditScore(enterpriseId: number) {
  return request.post(`/credit/calculate/${enterpriseId}`)
}
