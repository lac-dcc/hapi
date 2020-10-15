function post(endpoint, data) {
  return fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  })
    .then(res => res.json());
}

function genDot(svgId, dot) {
  var viz = new Viz();

  const svgDiv = $(`div#${svgId}`);
  svgDiv.html("");

  viz.renderSVGElement(dot)
    .then(function (svg) {
      svgDiv.append(svg);
    })
    .catch(error => {
      svgDiv.html("Error");

      // Create a new Viz instance (@see Caveats page for more info)
      viz = new Viz();

      // Possibly display the error
      console.error(error);
    });
}

$(document).ready(function () {

  $("textarea#yaml").val("");

  $("#generate").click(function (e) {

    const legalease = $('textarea#legalease').val();
    const actors = $('textarea#actors').val();
    const resources = $('textarea#resources').val();
    const actions = $('textarea#actions').val();

    const data = {
      legalease,
      actors,
      resources,
      actions
    };

    post('generate', data).then(({ error, ...res }) => {
      if (error) {
        return alert(error);
      }

      $("textarea#yaml").val(res.legalease);
      genDot("actorsGraph", res.actors);
      genDot("resourcesGraph", res.resources);
      genDot("actionsGraph", res.actions);

      const datamap = JSON.parse(res.datamap.replace(/'/g, '"'))["datamap"];

      datamap["Actors"].forEach(actor => {
        $("#actorsQuerie").append(new Option(actor, actor));
      })
      datamap["Resources"].forEach(resource => {
        $("#resourcesQuerie").append(new Option(resource, resource));
      })
      datamap["Actions"].forEach(action => {
        $("#actionsQuerie").append(new Option(action, action));
      })

      $("#matrix").load("output/legalease.html");

    });

    e.preventDefault();
  });

  $("#verify").click(function (e) {
    alert("WIP");
    e.preventDefault();
  });
});



