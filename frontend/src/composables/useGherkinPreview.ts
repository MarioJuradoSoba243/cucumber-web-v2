import type { FeatureDocument } from '../types/feature'

export function useGherkinPreview() {
  function render(feature: FeatureDocument | null): string {
    if (!feature) return ''
    const lines: string[] = []
    if (feature.tags?.length) lines.push(feature.tags.map((t) => `@${t}`).join(' '))
    lines.push(`Feature: ${feature.name}`)
    if (feature.description) {
      for (const line of feature.description.split(/\r?\n/)) lines.push(`  ${line}`)
      lines.push('')
    }
    if (feature.background) {
      lines.push(`  Background: ${feature.background.name || ''}`.trimEnd())
      feature.background.steps.forEach((s) => lines.push(`    ${capitalize(s.keyword)} ${s.text}`))
      lines.push('')
    }
    feature.scenarios.forEach((scenario) => {
      if (scenario.tags?.length) lines.push(`  ${scenario.tags.map((t) => `@${t}`).join(' ')}`)
      lines.push(`  ${scenario.type === 'OUTLINE' ? 'Scenario Outline' : 'Scenario'}: ${scenario.name}`)
      scenario.steps.forEach((s) => lines.push(`    ${capitalize(s.keyword)} ${s.text}`))
      if (scenario.type === 'OUTLINE') {
        lines.push('')
        scenario.exampleTables.forEach((table) => {
          if (table.tags?.length) lines.push(`    ${table.tags.map((t) => `@${t}`).join(' ')}`)
          lines.push(`    Examples:${table.name ? ` ${table.name}` : ''}`)
          lines.push(`      | ${table.columns.join(' | ')} |`)
          table.rows.forEach((r) => lines.push(`      | ${table.columns.map((c) => r.values[c] || '').join(' | ')} |`))
          lines.push('')
        })
      }
      lines.push('')
    })

    return lines.join('\n').trimEnd() + '\n'
  }

  function capitalize(k: string) {
    return k.charAt(0).toUpperCase() + k.slice(1).toLowerCase()
  }

  return { render }
}
