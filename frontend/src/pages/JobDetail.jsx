import { useParams } from 'react-router-dom'
import JobPanel from '../components/JobPanel'

export default function JobDetail() {
  const { id } = useParams()
  return <div className="card"><JobPanel id={id} /></div>
}
