var urlRestGlobal = "http://vps207605.ovh.net:8080/xwims-webservice";
var urlWebappGlobal = "http://vps207605.ovh.net:8080/xwims-webapp";
//var urlRestGlobal = "http://vps207605.ovh.net:8080/xwims-webservice";

function getParameterByName(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


 function functiontofindIndexByKeyValue(arraytosearch, key, valuetosearch) {
        for (var i = 0; i < arraytosearch.length; i++) {
            if (arraytosearch[i][key] == valuetosearch) {
                return i;
            }
        }
        return -1;
    }
