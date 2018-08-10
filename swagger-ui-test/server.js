const swaggerUiPath = require('swagger-ui-dist').absolutePath();
const express = require('express');

// server for swagger
const app = express();

// This line first to override what's inside swaggerUiPath
app.use(express.static('api'));
app.use(express.static(swaggerUiPath));
app.listen(8080, () =>
  console.log('Server listening on http://localhost:8080!')
);
