[#ftl][#include "/ftl/header.ftl"]

<table border="1" cellpadding="3">
	<tr><th> ${year?c} Year Song </th><th> Artist </th></tr>
	[#list yearTracks as yearTrack]
		<tr>
			<td> <a href="/songs/one?id=${yearTrack.id?c}">${yearTrack.name}</a> </td>
			<td> <a href="/artists/one?name=${yearTrack.artist}">${yearTrack.artist}</a> </td>
		</tr>
	[/#list]
</table>

[#include "/ftl/footer.ftl"]
