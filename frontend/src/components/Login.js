import React, { useState, useEffect, useRef } from 'react';
import axios from '../axiosSetup';
import { useNavigate, Link } from 'react-router-dom';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

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

      // ✅ Block unverified users
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

      if (response.data && response.data.userId) {
        setUserId(response.data.userId);
        navigate('/matchlist');
      } else {
        setMessage("Login failed");
      }
    } catch (error) {
      if (isMounted.current) {
        const backendError = error.response?.data;
        if (typeof backendError === 'string') {
          setMessage(backendError);
        } else if (typeof backendError === 'object') {
          setMessage(backendError.error || backendError.message || 'Login failed');
        } else {
          setMessage('Login failed');
        }
      }
    } finally {
      if (isMounted.current) setIsSubmitting(false);
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

      {message && <p style={styles.message}>{message}</p>}

      <p style={{ marginTop: '1.5rem' }}>
        Need to create an account?{' '}
        <Link to="/signup" style={styles.link}>Sign up here</Link>
      </p>
    </div>
  );
}