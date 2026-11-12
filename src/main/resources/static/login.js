async function prepareAdminLogin() {
  const notice = document.querySelector('#loginNotice');
  const query = new URLSearchParams(location.search);
  if (query.has('error')) {
    notice.hidden = false;
    notice.textContent = 'Sign in was not successful. Check the username and password configured in STS.';
    notice.classList.add('login-error');
  } else if (query.has('logout')) {
    notice.hidden = false;
    notice.textContent = 'You have been signed out.';
  }
  try {
    const response = await fetch('/api/csrf', { headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error('CSRF token unavailable');
    const csrf = await response.json();
    document.querySelector('#csrfField').value = csrf.token;
  } catch (_) {
    notice.hidden = false;
    notice.textContent = 'Secure sign-in could not be prepared. Refresh the page and try again.';
    notice.classList.add('login-error');
    document.querySelector('#loginSubmit').disabled = true;
  }
}
prepareAdminLogin();
