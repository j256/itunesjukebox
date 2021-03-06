[#ftl][#include "/ftl/header.ftl"]

<table>
	<tr><td align="right"> Song Name: </td><td> ${track.name}
		[#if track.video ]
			<img src="/images/video.png" width="32" height="27" alt="Video" />
		[/#if]
	</td></tr>
	<tr><td align="right"> Artist: </td><td> <a href="/artists/one?name=${track.artist?url}" >${track.artist}</a> </td></tr>
	<tr><td align="right"> Genre: </td><td> <a href="/genres/one?name=${track.genre?url}" >${track.genre}</a>
		[#if track.year > 0 ]
			from <a href="/years/one?year=${track.year?c}">${track.year?c}</a>
		[/#if]
	</td></tr>
	<tr><td align="right"> # Votes: </td><td> <span id="voteCount">${track.voteCount?c}</span> <span id="voted" style="display:none;"><b>voted</b></span> </td></tr>
	<tr><td align="right"> # Plays: </td><td> ${track.playCount?c} </td></tr>
</table>

<p>
<img id="voteUp" src="/images/vote_up.png" height="64" alt="Vote for song" />
[#if admin ]
	<img id="voteDown" src="/images/vote_down.png" height="64" alt="Vote down for song" />
[/#if]
</p>
<!-- NOTE: because we dynamically load the artwork, we can't test for it -->
<p> <img src="/artwork?id=${track.id?c}" alt="Album artwork" height="180" /> </p>

<script>
	var voteCount = ${track.voteCount?c};
	$("#voteUp").click(function(){ vote(true); }).mousedown(function() { $("#vote").fadeOut(50).fadeIn(50); }).css("cursor", "pointer");
	$("#voteDown").click(function(){ vote(false); }).mousedown(function() { $("#voteDown").fadeOut(50).fadeIn(50); }).css("cursor", "pointer");
	function vote(up) {
		if (up) {
			voteCount++;
		} else {
			voteCount--;
			if (voteCount < 0) {
				voteCount = 0;
			}
		}
		console.log( "Clicked vote button.  voteCount = " + voteCount );
		$("#voteCount").html(voteCount).fadeOut(50).fadeIn(50);
		$("#voted").show();
		$.ajax({
			url: "/ajax/vote?id=${track.id?c}&up=" + up,
			cache: false,
			success: function(response) {
				console.log("Server response: " + response );
			}
		});
	}
</script>

[#include "/ftl/footer.ftl"]
