[#ftl][#include "/ftl/header.ftl"]

<p> <a href="/" >Menu</a> </p>

<table border="1" cellpadding="3">
	<tr><th> Year </th><th> # Songs </th></tr>
	[#list yearInfos as yearInfo]
		<tr>
			<td> <a href="/years/one?year=${yearInfo.name?url}">${yearInfo.name}</a> </td>
			<td align="right"> ${yearInfo.numTracks?c} </td>
		</tr>
	[/#list]
</table>

[#include "/ftl/footer.ftl"]
