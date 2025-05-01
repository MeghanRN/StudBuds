// frontend/src/components/Login.js
import React, { useState, useEffect, useRef } from 'react';
import axios from '../axiosSetup';
import { useNavigate, Link } from 'react-router-dom';
import {
  getAuth,
  signInWithEmailAndPassword,
  sendPasswordResetEmail
} from 'firebase/auth';

export default function Login({ setUserId }) {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [message, setMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const isMounted = useRef(true);
  const auth = getAuth();

  useEffect(() => {
    return () => { isMounted.current = false; };
  }, []);

  const handleChange = e => {
    const value = e.target.name === 'email'
      ? e.target.value.toLowerCase()
      : e.target.value;
    setFormData(fd => ({ ...fd, [e.target.name]: value }));
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setMessage('');
    setIsSubmitting(true);

    try {
      const userCredential = await signInWithEmailAndPassword(
        auth,
        formData.email,
        formData.password
      );

      if (!userCredential.user.emailVerified) {
        setMessage('Please verify your email before logging in.');
        await auth.signOut();
        return;
      }

      const token = await userCredential.user.getIdToken();
      const response = await axios.post(
        '/api/auth/login',
        { firebaseToken: token },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.data?.userId) {
        setUserId(response.data.userId);
        navigate('/matchlist');
      } else {
        setMessage('Login failed');
      }

    } catch (error) {
      if (!isMounted.current) return;
      if (error.code && error.code.startsWith('auth/')) {
        setMessage('Username or password not found');
      } else {
        const backendMsg = error.response?.data?.message || error.response?.data;
        setMessage(typeof backendMsg === 'string' ? backendMsg : 'Login failed');
      }
    } finally {
      if (isMounted.current) setIsSubmitting(false);
    }
  };

  const handleForgot = async () => {
    setMessage('');
    if (!formData.email) {
      setMessage('Please enter your email to reset password.');
      return;
    }
    try {
      await sendPasswordResetEmail(auth, formData.email);
      setMessage('Password reset email sent. Check your inbox.');
    } catch (error) {
      if (error.code === 'auth/user-not-found') {
        setMessage('Email not found.');
      } else {
        setMessage(error.message || 'Failed to send reset email.');
      }
    }
  };

  const styles = {
    container: {
      maxWidth: '400px',
      margin: '3rem auto',
      padding: '2rem',
      borderRadius: '12px',
      backgroundColor: '#ffffff',
      boxShadow: '0 6px 12px rgba(0,0,0,0.1)',
      textAlign: 'center',
    },
    heading: {
      color: '#2c6e6a',
      marginBottom: '1.5rem',
    },
    input: {
      width: '100%',
      padding: '12px',
      margin: '0.5rem 0',
      borderRadius: '20px',
      border: '1px solid #ccc',
      boxSizing: 'border-box',
      outline: 'none',
      fontSize: '1rem',
    },
    button: {
      width: '100%',
      padding: '12px',
      marginTop: '1rem',
      borderRadius: '20px',
      border: 'none',
      backgroundColor: '#5ccdc1',
      color: '#fff',
      fontSize: '1rem',
      cursor: 'pointer',
      transition: 'background-color 0.3s ease',
    },
    buttonDisabled: {
      backgroundColor: '#9de0da',
      cursor: 'not-allowed',
    },
    message: {
      color: 'blue',
      textAlign: 'center',
      marginTop: '1rem',
    },
    link: {
      color: '#2c6e6a',
      textDecoration: 'none',
      fontWeight: 'bold',
      background: 'none',
      border: 'none',
      padding: 0,
      margin: '0.5rem 0',
      cursor: 'pointer'
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.heading}>Log In to StudBuds</h2>
      <form onSubmit={handleSubmit}>
        <input
          style={styles.input}
          type="email"
          name="email"
          placeholder="Email (@cooper.edu)"
          onChange={handleChange}
          value={formData.email}
          required
        />
        <input
          style={styles.input}
          type="password"
          name="password"
          placeholder="Password"
          onChange={handleChange}
          value={formData.password}
          required
        />
        <button
          type="submit"
          style={{
            ...styles.button,
            ...(isSubmitting ? styles.buttonDisabled : {})
          }}
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Logging In...' : 'Log In'}
        </button>
      </form>

      {/* Forgot password */}
      <button onClick={handleForgot} style={styles.link}>
        Forgot password?
      </button>

      {message && <p style={styles.message}>{message}</p>}

      <p style={{ marginTop: '1.5rem' }}>
        Need to create an account?{' '}
        <Link to="/signup" style={styles.link}>Sign up here</Link>
      </p>
    </div>
  );
}