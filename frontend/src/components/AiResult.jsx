function parse(json) {
  try { return JSON.parse(json) } catch { return null }
}

export function Score({ value, label }) {
  if (value == null) return null
  const tone = value >= 75 ? 'good' : value >= 50 ? 'mid' : 'low'
  return (
    <div className={`score ${tone}`} title={label}>
      <svg viewBox="0 0 36 36" aria-hidden="true">
        <path className="ring-bg" d="M18 2.5a15.5 15.5 0 1 1 0 31 15.5 15.5 0 1 1 0-31" />
        <path className="ring" strokeDasharray={`${value}, 100`} d="M18 2.5a15.5 15.5 0 1 1 0 31 15.5 15.5 0 1 1 0-31" />
      </svg>
      <span>{value}</span>
    </div>
  )
}

const Chips = ({ items, tone }) => (items?.length ? <div className="chips">{items.map((s, i) => <span key={i} className={`chip ${tone || ''}`}>{s}</span>)}</div> : null)
const List = ({ title, items }) => (items?.length ? <div className="block"><h4>{title}</h4><ul>{items.map((s, i) => <li key={i}>{s}</li>)}</ul></div> : null)

/** Shows one saved AI result. `analysis` is the API's AnalysisDto. */
export default function AiResult({ analysis }) {
  const d = parse(analysis.resultJson)
  if (!d) return <pre className="raw">{analysis.resultJson}</pre>

  if (analysis.type === 'ANALYSIS') return (
    <div className="ai">
      <div className="ai-head"><Score value={analysis.score ?? d.overallScore} label="Resume score" />
        <div><h3>Resume analysis</h3><p>{d.summary}</p>{d.experienceLevel && <span className="chip">{d.experienceLevel}</span>}</div></div>
      <div className="block"><h4>Skills found</h4><Chips items={d.skills} /></div>
      <div className="cols"><List title="Strengths" items={d.strengths} /><List title="Weaknesses" items={d.weaknesses} /></div>
      <List title="Missing sections" items={d.missingSections} />
      <div className="block"><h4>Suggested roles</h4><Chips items={d.suggestedRoles} tone="blue" /></div>
    </div>
  )

  if (analysis.type === 'MATCH') return (
    <div className="ai">
      <div className="ai-head"><Score value={analysis.score ?? d.matchScore} label="Match score" />
        <div><h3>Job match{analysis.jobTitle ? `: ${analysis.jobTitle}` : ''}</h3>
          {d.verdict && <span className={`chip ${d.verdict === 'Strong' || d.verdict === 'Good' ? 'green' : 'amber'}`}>{d.verdict} fit</span>}
          <p>{d.summary}</p></div></div>
      <div className="cols">
        <div className="block"><h4>Matched skills</h4><Chips items={d.matchedSkills} tone="green" /></div>
        <div className="block"><h4>Missing skills</h4><Chips items={d.missingSkills} tone="red" /></div>
      </div>
      <div className="cols"><List title="Strengths" items={d.strengths} /><List title="Gaps" items={d.gaps} /></div>
      {d.recommendation && <div className="note"><b>Recommendation:</b> {d.recommendation}</div>}
    </div>
  )

  if (analysis.type === 'INTERVIEW') return (
    <div className="ai">
      <h3>Interview questions{analysis.jobTitle ? `: ${analysis.jobTitle}` : ''}</h3>
      <ol className="questions">
        {(d.questions || []).map((q, i) => (
          <li key={i}>
            <p className="q">{q.question}</p>
            <div className="chips"><span className="chip blue">{q.category}</span><span className="chip">{q.difficulty}</span></div>
            {q.hint && <p className="hint">💡 {q.hint}</p>}
          </li>
        ))}
      </ol>
    </div>
  )

  return (
    <div className="ai">
      <h3>Resume improvements{analysis.jobTitle ? ` for ${analysis.jobTitle}` : ''}</h3>
      <p>{d.summary}</p>
      {(d.improvements || []).map((x, i) => (
        <div className="improve" key={i}>
          <h4>{x.section}</h4>
          <p><b>Issue:</b> {x.issue}</p>
          <p><b>Fix:</b> {x.suggestion}</p>
          {x.example && <p className="example">{x.example}</p>}
        </div>
      ))}
      {d.rewrittenSummary && <div className="note"><b>Suggested summary:</b> {d.rewrittenSummary}</div>}
      <div className="block"><h4>Keywords to add</h4><Chips items={d.keywordsToAdd} tone="blue" /></div>
      <List title="Formatting tips" items={d.formattingTips} />
    </div>
  )
}
