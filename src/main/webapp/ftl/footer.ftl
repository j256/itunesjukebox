[#ftl]
[#if (! initialized??) || initialized ]
	<p> <a href="/" >Menu</a> <a href="/search.ftl" >Search</a> <a href="/songs/" >Tracks</a> </p>
	<p> <a href="/artists/" >Artists</a> <a href="/genres/" >Genres</a> <a href="/videos.ftl" >Videos</a> </p>
	<p> <a href="/years/" >Years</a> <a href="/songs/played" >Played</a> </p>
	
	[#if endLinks?has_content ]
		${endLinks}
	[/#if]
[/#if]

</body>
</html>
