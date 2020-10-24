<!DOCTYPE html>

<html>
	<head>
		<title>Index view</title>
	</head>

	<body>
		Please choose a game!
		<!-- Set up the buttons for the 3 different Games -->
		<form action = "${pageContext.servletContext.contextPath}/addNumbers">
			<button type ="submit">addNumbers</button>
		</form>
		
        <form action = "${pageContext.servletContext.contextPath}/MultiplyNumbers">
			<button type ="submit">MultiplyNumbers</button>
		</form>
		
		<form action = "${pageContext.servletContext.contextPath}/guessingGame">
			<button type ="submit">guessingGame</button>
		</form>
		
	</body>
</html>
