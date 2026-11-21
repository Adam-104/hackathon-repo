import { useAuth } from '../auth'
import Jobs from './Jobs'
import Landing from './Landing'

/** Guests see the welcome page; signed-in users go straight to their job board. */
export default function Home() {
  const { user } = useAuth()
  return user ? <Jobs /> : <Landing />
}
