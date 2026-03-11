/**
 * 获取信用等级对应的颜色
 */
export function getGradeColor(grade: string): string {
  const map: Record<string, string> = {
    AAA: '#059669', AA: '#16a34a', A: '#22c55e',
    BBB: '#2563eb', BB: '#eab308', B: '#ea580c',
    C: '#dc2626', D: '#991b1b'
  }
  return map[grade] || '#94a3b8'
}

/**
 * 获取风险等级对应的颜色
 */
export function getRiskColor(level: string): string {
  const map: Record<string, string> = {
    HIGH: '#dc2626', MEDIUM: '#ea580c', LOW: '#eab308', NONE: '#16a34a'
  }
  return map[level] || '#94a3b8'
}

/**
 * 获取风险等级的标签
 */
export function getRiskLabel(level: string): string {
  const map: Record<string, string> = {
    HIGH: '高危预警', MEDIUM: '中危预警', LOW: '低危提示', NONE: '暂无风险'
  }
  return map[level] || level
}

/**
 * 获取进度条颜色
 */
export function getProgressColor(pct: number): string {
  if (pct >= 80) return '#16a34a'
  if (pct >= 60) return '#2563eb'
  if (pct >= 40) return '#eab308'
  return '#dc2626'
}

/**
 * 获取整体印象文字
 */
export function getImpressionText(val: number): string {
  const map: Record<number, string> = { 4: '非常好', 3: '不错', 2: '一般', 1: '较差' }
  return map[val] || ''
}

/**
 * 获取整体印象图标
 */
export function getImpressionIcon(val: number): string {
  const map: Record<number, string> = { 4: '😊', 3: '🙂', 2: '😐', 1: '😟' }
  return map[val] || ''
}

/**
 * 获取欠款等级文字
 */
export function getOverdueLabel(level: string): string {
  const map: Record<string, string> = {
    NORMAL: '正常', MILD: '轻度超期', MODERATE: '中度超期',
    SEVERE: '严重超期', MALICIOUS: '恶意欠款'
  }
  return map[level] || level
}

/**
 * 获取欠款等级颜色
 */
export function getOverdueColor(level: string): string {
  const map: Record<string, string> = {
    NORMAL: '#16a34a', MILD: '#eab308', MODERATE: '#ea580c',
    SEVERE: '#dc2626', MALICIOUS: '#991b1b'
  }
  return map[level] || '#94a3b8'
}

/**
 * 数据充分度数字转为填充块
 */
export function getSufficiencyBlocks(level: number): boolean[] {
  return Array.from({ length: 5 }, (_, i) => i < level)
}
