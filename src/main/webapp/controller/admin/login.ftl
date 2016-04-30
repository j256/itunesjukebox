[#ftl][#include "/ftl/header.ftl"]

<h1> iTunesJukeBox Login </h1>

[#if ! initialized ]
	<p>You need to choose an admin password for the application.  You can then login to the admin section of
	this application from other devices and enter the same password to get special permissions.</a></p>
[/#if]

<form method="post" action="submit">
	Admin password: <input name="password" type="string" /> <input type="submit" />
</form>

[#include "/ftl/footer.ftl"]
