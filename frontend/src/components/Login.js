// src/Login.js
import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

function Login({ setUserId }) {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const auth = getAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Sign in with Firebase
      const userCredential = await signInWithEmailAndPassword(
        auth,
        formData.email,
        formData.password
      );
      const token = await userCredential.user.getIdToken();

      // Call backend login endpoint
      const response = await axios.post(
        '/api/auth/login',
        { firebaseToken: token },
        { headers: { 'Authorization': `Bearer ${token}` } }
      );

      if (response.data && response.data.userId) {
        setUserId(response.data.userId);
        navigate('/dashboard');
      } else {
        setMessage("Login failed: userId not returned.");
      }
    } catch (error) {
      setMessage(error.response?.data || error.message || 'Login failed');
    }
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '2rem' }}>
      <h2>Log In</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="Email (@cooper.edu)"
          onChange={handleChange}
          value={formData.email}
          required
        /><br/><br/>
        <input
          type="password"
          name="password"
          placeholder="Password"
          onChange={handleChange}
          value={formData.password}
          required
        /><br/><br/>
        <button type="submit">Log In</button>
      </form>
      {message && <p style={{ color: 'red' }}>{message}</p>}
      <p>
        Need to create an account? <Link to="/signup">Sign up here</Link>
      </p>
    </div>
  );
}

export default Login;