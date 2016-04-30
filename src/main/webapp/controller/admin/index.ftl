[#ftl][#include "/ftl/header.ftl"]

<h1> iTunesJukeBox Admin </h1>

[#if ! initialized ]
	<p><a href="playlists/">To start you need to choose a playlist to initialize.</a></p>
[/#if]

<p>
	<img id="pause" src="/images/pause.png" width="64" height="64" alt="Pause" />
	<img id="play" src="/images/play.png" width="64" height="64" alt="Play" />
	<img id="nextTrack" src="/images/next_track.png" width="64" height="64" alt="Next Track" /><br />
	<img id="volumeDown" src="/images/volume_down.png" width="64" height="64" alt="Volume Down" />
	<img id="volumeUp" src="/images/volume_up.png" width="64" height="64" alt="Volume Up" />
</p>

<script>
	$("#pause").click(function() { ajax("pause"); })
		.css("cursor", "pointer").mousedown(function() { $("#pause").fadeOut(50).fadeIn(50); });
	$("#play").click(function() { ajax("play"); })
		.css("cursor", "pointer").mousedown(function() { $("#play").fadeOut(50).fadeIn(50); });
	$("#nextTrack").click(function() { ajax("nextTrack"); })
		.css("cursor", "pointer").mousedown(function() { $("#nextTrack").fadeOut(50).fadeIn(50); });
	$("#volumeUp").click(function() { ajax("volumeUp"); })
		.css("cursor", "pointer").mousedown(function() { $("#volumeUp").fadeOut(50).fadeIn(50); });
	$("#volumeDown").click(function() { ajax("volumeDown"); })
		.css("cursor", "pointer").mousedown(function() { $("#volumeDown").fadeOut(50).fadeIn(50); });
	function ajax(command) {
		console.log("Command: " + command);
		$.get( "/ajax/" + command, function(response) { console.log("Server response: " + response ); });
	}
</script>

[#include "/ftl/footer.ftl"]
