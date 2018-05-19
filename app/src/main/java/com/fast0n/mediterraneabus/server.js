const express = require('express');
const app = express();
const request = require('request');

var id = "hOsBuh2OXLQC";
var lpg = "PP1";
var dq = "cardiovascular system";
var pg = "PR3";
var jscmd = "click3";

app.get('/', function (req, res) {

    var options = {
        uri: 'https://books.google.it/books?id=' + id + '&lpg=' + lpg + '&dq=' + dq + '&pg=' + pg + '&jscmd=' + jscmd + '&vq=' + dq + '',
        method: 'GET',
        json: true,
        referer: 'https://books.google.it/books?id=hOsBuh2OXLQC&printsec=frontcover&dq=cardiovascular+system&hl=it&sa=X&ved=0ahUKEwj0z-n-iN3aAhXBWhQKHfXDCVEQ6AEIKDAA'
    };

    request(options, function (error, response, body) {
        if (!error && response.statusCode == 200) {

            var array = [];
            res.send(body);

            for (var i = 0; i < body['page'].length; i++) {
                array = array.concat(body['page'][i]['pid']);
            }

            res.send(array)
        }
        /*
        else
            res.sendStatus(response.statusCode)
        */

    });
});

exports = module.exports = app;
const server = app.listen(process.env.PORT, function () {});