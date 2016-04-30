[#ftl][#assign headerStuff = "<script src=\"/js/menu.js\"></script>" ][#include "/ftl/header.ftl"]

[#if initialized ]
	<table border="1" cellpadding="3">
		<tbody id="menuRows">
			<tr><th> Playing Name </th><th> Artist </th><th> Votes </th></tr>
		</tbody>
	</table>
	[#assign endLinks = "<p> <a href=\"/admin/\" >Admin</a> </p>" ]
[#else]
	<p> Welcome to the itunesjukebox Java program.  Before we can start playing some tracks you will need to: </p>
	<ol>
	<li> Enter an admin password which will allow you to control the jukebox. </li>
	<li> Choose a playlist from which tracks will be shown to your users. </li>
	</ol> 
	<a href="/admin/playlists/">Get Started</a>
[/#if]

[#include "/ftl/footer.ftl"]
