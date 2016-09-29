<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<title>CNN News</title>
				<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen"/>
				<link href="bootstrap/css/font-awesome.min.css" rel="stylesheet" media="screen"/>
				<link href="bootstrap/css/style.css" rel="stylesheet" media="screen"/>
				<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
				<script language="javascript" type="text/javascript" src="bootstrap/js/transition.js"></script>
				<script language="javascript" type="text/javascript" src="bootstrap/js/collapse.js"></script>
				<script language="javascript" type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
				<script language="javascript" type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
			</head>
			<body>
				<!-- HEADER -->
				<nav class="navbar navbar-default" role="navigation">
					<div class="container">
						<!-- Logo CNN -->
						<div class="navbar-header">
							<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-menu">
								<i class="icon-reorder"></i>
							</button>
							<a class="navbar-brand" href="#"><img src="bootstrap/img/cnn_logo.gif"/></a>
						</div>
						<!-- Regiões -->
						<div class="collapse navbar-collapse navbar-menu">
							<ul class="nav navbar-nav navbar-right">
								<li><a href="#US">US</a></li>
								<li><a href="#AFRICA">AFRICA</a></li>
								<li><a href="#ASIA">ASIA</a></li>
								<li><a href="#EUROPE">EUROPE</a></li>
								<li><a href="#LATINAMERICA">LATIN AMERICA</a></li>
								<li><a href="#MIDDLEEAST">MIDDLE EAST</a></li>
							</ul>
						</div>
					</div>
				</nav>

				<!-- CONTEUDO -->
				<xsl:for-each select="cnn/region">
					<!-- Região -->
					<h1 id="{@name}"><xsl:value-of select="@name"/></h1>
					<div class="panel-group" id="accordion">
						<!-- Notícias -->
						<xsl:for-each select="news">
							<div class="panel panel-default">
								<div class="panel-heading">
									<!-- Título da notícia -->
							    	<h3 class="panel-title">
							    		<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse{../@name}{position()}">
							    			<xsl:value-of select="title"/>
							    		</a>
							    	</h3>
							  	</div>
							  	<div id="collapse{../@name}{position()}" class="panel-collapse collapse">
								  	<div class="panel-body">
								  		<!-- Autor(es) -->
								  		<xsl:if test="count(author)>0">
									  		by
									  		<xsl:choose>
												<xsl:when test="count(author)=1">
													<b><xsl:value-of select="author"/>, </b>
												</xsl:when >
												<xsl:when test="count(author)=2">
													<b><xsl:value-of select="author[1]"/></b> and <b><xsl:value-of select="author[2]"/>, </b>
												</xsl:when >
												<xsl:otherwise>
													<b><xsl:value-of select="author[1]"/></b>, <b><xsl:value-of select="author[2]"/></b> and <b><xsl:value-of select="author[3]"/>, </b>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<!-- Data -->
										on
										<xsl:value-of select="date/day"/>/<xsl:value-of select="date/month"/>/<xsl:value-of select="date/year"/>
										<br/><br/>
										<!-- Highlights -->
								    	<ul>
								    		<xsl:for-each select="highlights">
								    			<li><xsl:value-of select="."/></li>
								    		</xsl:for-each>
								    	</ul>
								    	<!-- Foto -->
								    	<div class="row">
										  	<div class="col-xs-5">
										  		<xsl:if test="photoURL != ''">
									  				<a href="{photoURL}" class="thumbnail">
											      		<img src="{photoURL}"/>
											    	</a>
										  		</xsl:if>
										  	</div>
										</div>
										<br/>
										<!-- Video -->
										<div class="video">
											<xsl:if test="videoURL != ''">
									  			<!-- <iframe width="256px" height="144px" src="http://edition.cnn.com/video/api/embed.html#/video/world/2014/10/15/deadly-disasters-in-the-himalayas-ts-orig.cnn" frameborder="1"></iframe> -->
									  			<a class="btn btn-primary btn-lg" role="button" href="{videoURL}"><span class="glyphicon glyphicon-film"></span> Video</a>
									  		</xsl:if>
										</div>
										<br/>
										<!-- Texto da notícia -->
										<div class="texto">
											<xsl:value-of select="text"></xsl:value-of>
										</div>
										<br/>
										<!-- Fonte -->
										<div class="source">
											Source: <a href="{url}" style="color: #FF0000" target="_blank">CNN</a>
										</div>
								  	</div>
								</div>
							</div>
						</xsl:for-each>
					</div>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>	
</xsl:stylesheet>