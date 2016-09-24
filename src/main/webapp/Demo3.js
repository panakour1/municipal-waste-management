/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
$(document).ready(function () {



    L.mapbox.accessToken = 'pk.eyJ1IjoicGFuaWthcyIsImEiOiJjaW41NWRqdW4wMHQydzhtMjk0c2V0ZnhzIn0.gNMfrN0tqj2779aQibum_A';
    var map = L.mapbox.map('map', 'mapbox.streets').setView([52.518735, 13.405702], 11);
    var coo;
    var marker = L.marker();
    var routeLayer;
    var masterLayer = L.geoJson();
    //var selectedMarker;
    //var newMarker;
    marker.setIcon(L.mapbox.marker.icon({
        'marker-size': 'small',
        //'marker-symbol': 'waste-basket',
        'marker-color': '#505050'
    }));

    var locations = L.mapbox.featureLayer().addTo(map);
    refreshPage();

    map.on('click', function (ev) {
        coo = ev.latlng;
        $("#lat").val(coo.lat);
        $("#lng").val(coo.lng);
        marker.setLatLng(coo);
        marker.addTo(locations);
    });

    $("#addBinButton").click(function () {
        $.post("Servlet3b", {
            action: "add",
            type: "bin",
            lat: coo.lat,
            lng: coo.lng,
            fullness: $("#fullness").val()
        }
        , function (data, status) {

            map.removeLayer(marker);
            //marker = L.marker();
            refreshPage();

        });
    });

    $("#addDepotButton").click(function () {
        $.post("Servlet3b", {
            action: "add",
            type: "depot",
            lat: coo.lat,
            lng: coo.lng,
            numOfVehicles: $("#numOfVehicles").val()
        }
        , function (data, status) {

            map.removeLayer(marker);
            //marker = L.marker();
            refreshPage();

        });
    });



    $("#deleteButton").click(function () {
        $.post("Servlet3b", {
            action: "delete",
            locationId: $("#deleteId").val()
        }, function (data, status) {
            //alert("done");
            refreshPage();
        });
    });

    $("#updateButton").click(function () {
        $.post("Servlet3b", {
            action: "update",
            binId: $("#updateId").val(),
            fullness: $("#updateFullness").val()
        }, function (data, status) {

            refreshPage();

        });
    });

    $("#optimiseButton").click(function () {

        $.post("Servlet3b", {
            action: "optimise"
        }, function (data, status) {


            masterLayer.clearLayers();
            for (i = 0; i < data.routes.length; i++) {
                routeLayer = L.geoJson(null, {
                    // http://leafletjs.com/reference.html#geojson-style
                    style: {color: data.routes[i].colour}
                });
                for (j = 0; j < data.routes[i].files.length; j++) {
                    omnivore.gpx(data.routes[i].files[j], null, routeLayer).addTo(masterLayer);
                }
            }
            masterLayer.addTo(map);
            //alert(data.bins[37].hasOwnProperty("position2"));
            locations.eachLayer(function (locale) {
                if (data.bins.hasOwnProperty(locale.feature.properties.locationId)) {
                    locale.setIcon(L.mapbox.marker.icon({
                        'marker-size': 'small',
                        'marker-symbol': data.bins[locale.feature.properties.locationId].position,
                        'marker-color': data.bins[locale.feature.properties.locationId].colour
                    }));
                }

            });



        });


    });

    function refreshPage() {

        $.post("Servlet3a", {}, function (data, status) {
            locations.setGeoJSON(data);
            locations.eachLayer(function (locale) {
                if (locale.feature.properties.type == "bin")
                {
                    if (locale.feature.properties.fullness < 50)
                    {
                        locale.setIcon(L.mapbox.marker.icon({
                            'marker-size': 'small',
                            'marker-symbol': 'waste-basket',
                            'marker-color': '#009900'
                        }));
                    } else
                    {
                        locale.setIcon(L.mapbox.marker.icon({
                            'marker-size': 'small',
                            'marker-symbol': 'waste-basket',
                            'marker-color': '#ff0000'
                        }));
                    }

                    locale.on('click', function (e) {
                        //alert(locale.feature.properties.id);
                        $("#deleteId").val(locale.feature.properties.locationId);
                        $("#updateId").val(locale.feature.properties.binId);
                        //selectedMarker = locale;
                    });
                } else if (locale.feature.properties.type == "depot")
                {
                    locale.setIcon(L.mapbox.marker.icon({
                        'marker-size': 'medium',
                        'marker-symbol': 'bus',
                        'marker-color': '#ffad33'
                    }));

                    locale.on('click', function (e) {
                        //alert(locale.feature.properties.id);
                        $("#deleteId").val(locale.feature.properties.locationId);
                        //$("#updateId").val(locale.feature.properties.binId);
                        //selectedMarker = locale;
                    });
                }

            });

        });
    }

});
