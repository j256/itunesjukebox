/**
 * Updates our what's playlist list every X millis.
 */

$(document).ready(function() {
	var intervalMillis = 3000;
	var refresh = function() {
		$.getJSON("/ajax/whatsPlaying", function(tracks) {
			var rows = [];
			tracks.forEach(function(track) {
				var row = "<tr><td> <a href=\"/songs/one?id=" + track.id + "\">" + track.name + "</a> ";
				if (track.isVideo) {
					row += "<img src=\"/images/video.png\" height=\"25\" alt=\"Video\" />";
				}
				row += "</td>"
					+ "<td> <a href=\"/artists/one?name=" + track.artist + "\" >" + track.artist + "</a> </td>"
					+ "<td>";
				if (track.voteCount.value > 0) {
					row += track.voteCount.value;
				} else {
					row += "&nbsp;"
				}
				row += "</td></tr>\n";
				rows.push(row);
			});
			$('#menuRows').html(rows);
		});
		setTimeout(function() {	refresh(); }, intervalMillis);
	};
	refresh();
});
