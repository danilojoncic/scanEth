# Use official Node.js image
FROM node:20-alpine

# Create app directory
WORKDIR /usr/src/app

# Copy package.json and install dependencies
COPY package*.json ./
RUN npm install --production

# Copy the rest of the app
COPY .. .

# Expose port
EXPOSE 3000

# Start the app
CMD ["npm", "start"]
