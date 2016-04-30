[#ftl][#include "/ftl/header.ftl"]

<p> Select playlist which holds the songs that will be played by the jukebox </p>
<p style="line-height: 200%;">
	[#list playLists as playList]
		<tr><td> <a href="chosen?playList=${playList}">${playList}</a> </td></tr>
	[/#list]
</p>

[#include "/ftl/footer.ftl"]
