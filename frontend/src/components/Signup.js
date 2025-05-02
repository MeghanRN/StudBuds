import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { auth } from '../firebase-config';
import {
  createUserWithEmailAndPassword,
  sendEmailVerification,
} from 'firebase/auth';
import axios from '../axiosSetup';

const majors = [
  'Electrical Engineering','Mechanical Engineering','Chemical Engineering',
  'Civil Engineering','General Engineering','Computer Science'
];

export default function Signup() {
  const [f, setF] = useState({name:'', email:'', password:'', major:'', year:''});
  const [agree, setAgree] = useState(false);
  const [msg, setMsg] = useState('');
  const [busy, setBusy] = useState(false);
  const nav = useNavigate();

  const onChange = e =>
    setF({...f, [e.target.name]:
      e.target.name==='email' ? e.target.value.toLowerCase() : e.target.value});

  const valid = () => {
    if (!f.email.endsWith('@cooper.edu'))      return 'Use a @cooper.edu email';
    if (f.password.length < 9)                 return 'Password ≥9 chars';
    const y = +f.year; if (y<2020||y>2050)     return 'Year 2020–2050';
    if (!majors.includes(f.major))             return 'Select a valid major';
    if (!agree)                                return 'You must agree to share';
    return '';
  };

  const onSubmit = async e => {
    e.preventDefault(); setMsg('');
    const err = valid(); if (err) { setMsg(err); return; }

    setBusy(true);
    try {
      /* 1) Firebase account */
      const cred = await createUserWithEmailAndPassword(auth, f.email, f.password);
      await sendEmailVerification(cred.user);

      /* 2) Local account */
      const token = await cred.user.getIdToken();
      await axios.post('/api/auth/signup', {
          email: f.email, password: f.password, name: f.name,
          major: f.major, year: f.year
        },
        { headers:{ Authorization:`Bearer ${token}` } });

      nav('/login');
    } catch (e) {
      setMsg(e.message);
    } finally { setBusy(false); }
  };

  /* ---------- JSX ---------- */
  const input = p => <input {...p} required style={{display:'block',margin:'6px 0',width:'100%'}}/>;
  return (
  <div style={{maxWidth:400,margin:'3rem auto',padding:20,boxShadow:'0 4px 8px #ccc'}}>
    <h2>Sign Up</h2>
    <form onSubmit={onSubmit}>
      {input({name:'name',placeholder:'Full name',value:f.name,onChange:onChange})}
      {input({type:'email',name:'email',placeholder:'Email',value:f.email,onChange:onChange})}
      {input({type:'password',name:'password',placeholder:'Password',value:f.password,onChange:onChange})}
      <select name="major" value={f.major} onChange={onChange} required
              style={{display:'block',margin:'6px 0',width:'100%'}}>
        <option value="">Select major</option>{majors.map(m=><option key={m}>{m}</option>)}
      </select>
      {input({type:'number',name:'year',placeholder:'Year',value:f.year,onChange:onChange})}

      <label style={{display:'block',margin:'8px 0'}}>
        <input type="checkbox" checked={agree} onChange={()=>setAgree(!agree)}/>
        &nbsp;I agree StudBuds may share my email with matches
      </label>

      <button disabled={busy||!agree} style={{width:'100%'}}>
        {busy?'Signing up…':'Sign Up'}
      </button>
    </form>
    {msg && <p style={{color:'red'}}>{msg}</p>}
    <p>Already have an account? <Link to="/login">Log in</Link></p>
  </div>);
}
