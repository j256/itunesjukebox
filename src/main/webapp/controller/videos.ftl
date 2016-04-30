[#ftl][#include "/ftl/header.ftl"]

<p> <a href="/" >Menu</a> </p>

<table border="1" cellpadding="3">
	<tr><th> Video <img src="/images/video.png" height="25" alt="Video" /> </th><th> Artist </th><th> Votes </th><th> Plays </th></tr>
	[#list videoTracks as track]
		<tr>
			<td> <a href="/songs/one?id=${track.id?c}">${track.name}</a> </td>
			<td> <a href="/artists/one?name=${track.artist?url}" >${track.artist}</a> </td>
			<td align="right">
				[#if track.voteCount > 0 ]
					${track.voteCount?c}
				[#else]
					&nbsp;
				[/#if]
			</td>
			<td align="right">
				[#if track.playCount > 0 ]
					${track.playCount?c}
				[#else]
					&nbsp;
				[/#if]
			</td>
		</tr>
	[/#list]
</table>

[#include "/ftl/footer.ftl"]
