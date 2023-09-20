package net.j2i.webcrawler

interface IContentExtractedHandler {

    fun PageLoadComplete();
    fun LinksExtracted(linkList:Array<String>);
    fun ResourceRequested(url:String)

}