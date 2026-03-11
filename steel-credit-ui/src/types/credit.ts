/** 统一响应体 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 数据来源项 */
export interface DataSourceItem {
  name: string
  available: boolean
  detail?: string
}

/** 核心指标 */
export interface CoreIndicator {
  name: string
  code: string
  score: number
  maxScore: number
  percentage: number
  level: string
}

/** 信用概要 */
export interface CreditSummary {
  creditGrade: string
  totalScore: number
  dataSufficiency: number
  dataSufficiencyLabel: string
  ratingMode: string
  dataSources: DataSourceItem[]
  coreIndicators: CoreIndicator[]
  overdueTag?: string
  overdueTagDesc?: string
}

/** 风险预警概况 */
export interface RiskSummaryVO {
  enterpriseId: number
  overallRiskLevel: string
  overallRiskLabel: string
  highRiskCount: number
  mediumRiskCount: number
  lowRiskCount: number
  totalRiskCount: number
  selfRiskCount: number
  relatedPartyRiskCount: number
  lastCheckDate: string
  disclaimer: string
}

/** 信用名片 */
export interface CreditCardVO {
  enterpriseId: number
  enterpriseName: string
  role: string
  buyerCredit?: CreditSummary
  sellerCredit?: CreditSummary
  riskSummary: RiskSummaryVO
}

/** 维度中的子指标得分 */
export interface IndicatorScore {
  indicatorCode: string
  indicatorName: string
  score: number
  maxScore: number
  rawValueDisplay?: string
}

/** 维度得分 */
export interface DimensionScore {
  dimensionCode: string
  dimensionName: string
  dataSource: string
  score: number
  maxScore: number
  percentage: number
  trend: string
  indicators?: IndicatorScore[]
}

/** 趋势点 */
export interface TrendPoint {
  period: string
  score: number
  grade: string
}

/** 等级变更 */
export interface GradeChange {
  period: string
  fromGrade: string
  toGrade: string
  fromScore: number
  toScore: number
}

/** 评价摘要 */
export interface FeedbackSummary {
  comment: string
  overallImpression: number
  evaluatorRegion?: string
  evaluatorType?: string
}

/** 信用详情 */
export interface CreditDetailVO {
  enterpriseId: number
  enterpriseName: string
  roleType: string
  creditGrade: string
  totalScore: number
  ratingMode: string
  ratingModeDesc: string
  dataSufficiency: number
  dataSufficiencyLabel: string
  dataSufficiencyDesc: string
  gradeCeiling: string
  ratingDate: string
  erpSourceCount: number
  feedbackCount: number
  dimensions: DimensionScore[]
  trend: TrendPoint[]
  gradeHistory: GradeChange[]
  feedbackSummaries: FeedbackSummary[]
  paymentTrend?: TrendPoint[]
  missingDataHints: string[]
  suggestions: string[]
}

/** 信用角标 */
export interface CreditBadgeVO {
  enterpriseId: number
  enterpriseName: string
  buyerGrade?: string
  buyerDataSufficiency?: number
  sellerGrade?: string
  sellerDataSufficiency?: number
  riskLevel?: string
  overdueTag?: string
  hasSevereOverdue?: boolean
}

/** 风险事件 */
export interface RiskEventItem {
  eventId: number
  riskCategory: string
  riskCategoryDesc: string
  riskLevel: string
  riskLevelLabel: string
  eventTitle: string
  eventDetail?: string
  involvedAmount?: number
  eventDate: string
  eventStatus: string
  relatedPartyName?: string
}

/** 关联方 */
export interface RelatedPartyItem {
  partyId: number
  partyName: string
  relationType: string
  relationTypeDesc: string
  shareholdingRatio: number
  relationLevel: number
  hasRisk: boolean
  riskCount?: number
}

/** 风险详情 */
export interface RiskDetailVO {
  enterpriseId: number
  enterpriseName: string
  summary: RiskSummaryVO
  selfRiskEvents: RiskEventItem[]
  relatedPartyRiskEvents: RiskEventItem[]
  relatedParties: RelatedPartyItem[]
  disclaimer: string
}

/** 合作评价 */
export interface CooperationFeedback {
  id: number
  evaluatorEnterpriseId: number
  targetEnterpriseId: number
  evaluatorRole: string
  cooperationLevel: number
  overallImpression: number
  paymentRating?: number
  pickupRating?: number
  orderStabilityRating?: number
  deliveryRating?: number
  qualityRating?: number
  pricingRating?: number
  communicationRating?: number
  willContinue?: number
  comment?: string
  triggerScene?: string
  createTime?: string
}

/** 评价提交请求 */
export interface FeedbackSubmitRequest {
  targetEnterpriseId: number
  cooperationLevel: number
  overallImpression: number
  paymentRating?: number
  pickupRating?: number
  orderStabilityRating?: number
  deliveryRating?: number
  qualityRating?: number
  pricingRating?: number
  communicationRating?: number
  willContinue?: number
  comment?: string
  triggerScene?: string
}
