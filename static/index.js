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

    const hapi = $('textarea#hapi').val();

    const data = {
      hapi
    };

    post('generate', data).then(({ error, ...res }) => {
      if (error) {
        return alert(error);
      }

      $("textarea#yaml").val(res.yaml);
      genDot("actorsGraph", res.actors);
      genDot("resourcesGraph", res.resources);
      genDot("actionsGraph", res.actions);

      $("#matrix").load("output/main.html");

    });

    e.preventDefault();
  });

  $("#verify").click(function (e) {
    alert("WIP");
    e.preventDefault();
  });
});



