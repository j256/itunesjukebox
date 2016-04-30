[#ftl][#include "/ftl/header.ftl"]

<h1> PlayList Chosen </h1>

<p> <a href="/">Menu</a> <a href="/admin/" >Admin</a> <p>

<p> The following tracks have been copied from the playlist '${playlist}' and are now available to be played
by the application.  The temporary playlist '${tempPlaylist}' has been created and cleared and will be used
to play and queue tracks. </p>

<table border="1">
	<tr><th> Id </th><th> Library Id </th><th> Name </th><th> Artist </th><th> Year </th><th> &nbsp; </th></tr>
	[#list tracks as track]
		<tr>
			<td align="right"> ${track.id?c} </td>
			<td align="right"> ${track.libraryId?c} </td>
			<td> ${track.name}
				[#if track.video]
					<img src="/images/video.png" height="25" alt="Video" />
				[/#if]
			</td>
			<td> ${track.artist} </td>
			<td align="right"> [#if track.year > 0 ] ${track.year?c} [#else] &nbsp; [/#if] </td>
			<td> ${track.video?string("yes", "")} </td>
		</tr>
	[/#list]
</table>

[#include "/ftl/footer.ftl"]
