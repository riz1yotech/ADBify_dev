# Fix Git 403 Permission Error

## Problem
```
remote: Permission to Riz1yotech/ADBify_dev.git denied to riz1yotech.
fatal: unable to access 'https://github.com/Riz1yotech/ADBify_dev.git/': The requested URL returned error: 403
```

## Root Cause
GitHub is using old credentials (`riz1yotech`) that don't have permission to access the repository. You need to authenticate as `Riz1yotech` or with proper credentials.

## Solution

### Method 1: Use Personal Access Token (Recommended)

#### Step 1: Generate Personal Access Token
1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Give it a descriptive name: `Adbify Development`
4. Set expiration (recommended: 90 days or No expiration for development)
5. Select scopes:
   - ✅ **repo** (Full control of repositories)
   - ✅ **workflow** (if using GitHub Actions)
6. Click **"Generate token"**
7. **COPY THE TOKEN IMMEDIATELY** - You won't see it again!

#### Step 2: Clear Old Credentials (Windows)
```bash
# Open Windows Credential Manager
Start → Search "Credential Manager" → Windows Credentials

# Remove old GitHub credentials:
1. Find entries starting with "git:https://github.com"
2. Click each one → Remove
```

Or use command line:
```bash
# Remove cached credentials
git config --global --unset credential.helper
git config --global credential.helper manager
```

#### Step 3: Use Token for Authentication

**Option A: Use token in URL (Temporary)**
```bash
cd C:\Users\yotech59\AndroidStudioProjects\Adbify

# Add token to remote URL (replace YOUR_TOKEN with actual token)
git remote set-url origin https://YOUR_TOKEN@github.com/Riz1yotech/ADBify_dev.git

# Now you can push/pull
git push origin main
```

**Option B: Use token when prompted (Recommended)**
```bash
cd C:\Users\yotech59\AndroidStudioProjects\Adbify

# When you try to push, Git will ask for credentials
git push origin main

# Enter:
# Username: Riz1yotech
# Password: [paste your Personal Access Token here]

# Windows Credential Manager will save these for future use
```

### Method 2: Use SSH Keys (Alternative)

#### Step 1: Generate SSH Key
```bash
# Open Git Bash or PowerShell
ssh-keygen -t ed25519 -C "your-email@example.com"

# Press Enter to accept default location: C:\Users\yotech59\.ssh\id_ed25519
# Enter a passphrase (optional but recommended)
```

#### Step 2: Add SSH Key to GitHub
```bash
# Copy public key
cat ~/.ssh/id_ed25519.pub
# Or on Windows:
type C:\Users\yotech59\.ssh\id_ed25519.pub
```

1. Go to: https://github.com/settings/keys
2. Click **"New SSH key"**
3. Title: `Adbify Dev Machine`
4. Paste the public key content
5. Click **"Add SSH key"**

#### Step 3: Change Remote to SSH
```bash
cd C:\Users\yotech59\AndroidStudioProjects\Adbify

# Change remote from HTTPS to SSH
git remote set-url origin git@github.com:Riz1yotech/ADBify_dev.git

# Test connection
ssh -T git@github.com

# Now you can push
git push origin main
```

## Quick Test

After setting up authentication, test with:

```bash
cd C:\Users\yotech59\AndroidStudioProjects\Adbify

# Check current branch
git branch

# Add files
git add .

# Commit
git commit -m "Add comprehensive documentation"

# Push (this should work now)
git push origin main
```

If you get prompted for credentials, use:
- **Username**: `Riz1yotech`
- **Password**: Your Personal Access Token (NOT your GitHub password)

## Verify Configuration

```bash
# Check remote URL
git remote -v

# Should show:
# origin  https://github.com/Riz1yotech/ADBify_dev.git (fetch)
# origin  https://github.com/Riz1yotech/ADBify_dev.git (push)

# Check Git user config
git config user.name   # Should be: Riz1yotech
git config user.email  # Should be your email
```

## Common Issues

### Issue: Token expired
**Solution**: Generate a new token and update credentials in Windows Credential Manager

### Issue: Still getting 403
**Solution**: 
1. Make sure you're using the correct account (`Riz1yotech`)
2. Clear ALL GitHub credentials in Windows Credential Manager
3. Try pushing again - enter correct username and NEW token

### Issue: Permission denied (SSH)
**Solution**: 
```bash
# Check SSH agent is running
eval "$(ssh-agent -s)"

# Add your SSH key
ssh-add ~/.ssh/id_ed25519
```

## Security Best Practices

1. ✅ Use Personal Access Tokens instead of passwords
2. ✅ Set token expiration dates
3. ✅ Use minimum required scopes (repo only)
4. ✅ Store tokens securely (use Credential Manager)
5. ✅ Never commit tokens to the repository
6. ✅ Rotate tokens regularly
7. ✅ Use SSH keys for better security

## Repository Owner Setup

If you ARE `Riz1yotech` (repository owner):
1. Make sure you're logged into the correct GitHub account in your browser
2. Use the Personal Access Token method above
3. Ensure the token has `repo` scope

If you're a COLLABORATOR:
1. Ask `Riz1yotech` to add you as a collaborator:
   - Repository → Settings → Collaborators → Add people
2. Accept the invitation email
3. Then use your own Personal Access Token

## Resources

- [GitHub Personal Access Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
- [GitHub SSH Keys](https://docs.github.com/en/authentication/connecting-to-github-with-ssh)
- [Git Credential Manager](https://github.com/git-ecosystem/git-credential-manager)

---

**Last Updated**: December 22, 2024

