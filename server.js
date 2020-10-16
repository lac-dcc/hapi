const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const { exec } = require('child_process');
const fs = require('fs');

const outputDir = "static/output";

const hapiFile = `${outputDir}/main.hp`;

// const program = "legalease/legalease.jar legalease/config.yaml";
const program = `bin/hapi.jar ${hapiFile}`;

const resultHapiFile = `${outputDir}/main.yaml`;
const resultActorsFile = `${outputDir}/actors.dot`;
const resultResourcesFile = `${outputDir}/resources.dot`;
const resultActionsFile = `${outputDir}/actions.dot`;

const app = express();
const jsonParser = bodyParser.json()

app.use(express.static('static'));

app.get('/', function (req, res) {
  res.sendFile(path.join(__dirname + '/static/index.html'));
});

app.post('/generate', jsonParser, function (req, res) {

  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir);
  }

  const { hapi } = req.body;

  fs.writeFileSync(hapiFile, hapi);

  exec(`java -jar ${program}`,
    (error, stdout, stderr) => {

      if (error || stderr) {
        console.log('stderr: ' + stderr);
        res.json({ error: { msg: stderr, ...error } });
        return;
      }

      const resultHapi = fs.readFileSync(resultHapiFile, "utf8");
      const resultActors = fs.readFileSync(resultActorsFile, "utf8");
      const resultResources = fs.readFileSync(resultResourcesFile, "utf8");
      const resultActions = fs.readFileSync(resultActionsFile, "utf8");

      res.json({
        yaml: resultHapi,
        actors: resultActors,
        resources: resultResources,
        actions: resultActions,
        datamap: stdout
      });
    });

});

app.post('/verify', jsonParser, function (req, res) {
  res.json({ api: 'verify' });
});

app.listen(8080, () => {
  console.log("server running!");
});