const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const { exec } = require('child_process');
const fs = require('fs');
const formidable = require('formidable');
const { exception } = require('console');

const outputDir = "./static/output/";

const hapiFile = `${outputDir}/main.hp`;

const program = `bin/hapi.jar`;

const resultHapiFile = `${outputDir}/main.yaml`;
const resultActorsFile = `${outputDir}/actors.dot`;
const resultResourcesFile = `${outputDir}/resources.dot`;
const resultActionsFile = `${outputDir}/actions.dot`;

const app = express();
const jsonParser = bodyParser.json()

app.use(express.static('static'));

function moveFiles(files){
  var oldpath = '';
  var newpath = '';
  
  for(key in files){
    oldpath = files[key].path;
    newpath = files[key].name;
    fs.rename(oldpath, outputDir+newpath, function (err) {
      if (err) throw err;
    });
  }
}

function removeOldFiles(dirPath) {
  try { var files = fs.readdirSync(dirPath); }
  catch(e) { return; }
  if (files.length > 0)
    for (var i = 0; i < files.length; i++) {
      var filePath = dirPath + '/' + files[i];
      if (fs.statSync(filePath).isFile())
        fs.unlinkSync(filePath);
    }
};

function FileReceivingError(message){
  this.message = 'Main file is required';
  this.name = 'InexistentMain'
}

Object.size = function(obj) {
  var size = 0, key;
  for (key in obj) {
      if (obj.hasOwnProperty(key)) size++;
  }
  return size;
}; 

app.post('/generate', function(req, res, next){

  var fs = require('fs');
  var form = new formidable.IncomingForm();
    form.parse(req, function (err, fields, files) {
      var mainFileName = undefined;
      try{
        if (!files.main)
          throw new FileReceivingError('Main file is required');
        if(Object.size(files) > 6)
          throw new FileReceivingError('Files quantity overflow');
          
        mainFile = files.main.name.substr(0, files.main.name.lastIndexOf("."));
        removeOldFiles(outputDir);
        moveFiles(files);
      } catch (e){
        res.status(500);
        res.json({ error: { msg: e.message}});
      }
      // mainFile = outputDir+files.main.name;
      exec(`java -jar ${program} ${outputDir+mainFile}.hp`,
        (error, stdout, stderr) => {
        if (error || stderr) {
          // console.log('stderr: ' + stderr);
          res.status(500);
          res.json({ error: { msg: stderr, ...error } });
          return;
        }

        const resultHapi = fs.readFileSync(`${outputDir+mainFile}.yaml`, "utf8");
        const resultActors = fs.readFileSync(resultActorsFile, "utf8");
        const resultResources = fs.readFileSync(resultResourcesFile, "utf8");
        const resultActions = fs.readFileSync(resultActionsFile, "utf8");
        const resultMatrix = fs.readFileSync(`${outputDir+mainFile}.html`, "utf8");

        res.json({
          yaml: resultHapi,
          actors: resultActors,
          resources: resultResources,
          actions: resultActions,
          matrix: resultMatrix,
          datamap: stdout
        });
      });    
    });
});

app.listen(8080, () => {
  console.log("server running!");
  if(!fs.existsSync(outputDir) ||
    (fs.existsSync(outputDir) && !fs.statSync(outputDir).isDirectory()))
    fs.mkdirSync(outputDir);
});