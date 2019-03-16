# EchoWrite

## Runs in Java.
Made for me, as I write my novels/stories in plain text, and then need to get them into Sigil EPUB to publish.

## Output, Capabilities
* Outputs via the "Formatter" a clean copy of the originial text with no DOCTAGS, can do both plaintext and HTML.
* Outputs files that contain only the tags you specify, [[*scene: xx *]] tags, or "character:"
* Can output many differnet such files, as you can configure via the UI.

## Output, Mine
* I have a character file, a scene file, a todo file, a clothing file, a knowledge file, etc.
* The HTML output in chapters can be brought into SIGIL easily to make a EPUB easily from text.
* No mucking about with Word or even LibreOffice conversions and cleaning of HTML

## DocTags
Recently added support for me to make comments in the text, which are used to create outlines and are stripped out of the final product. DocTags aren't counted in the word count or outputted in the cleaned plain text or html output.

```
-Example of text #1:
-= Chapter 12: Fallout =-
	[[*botday: still 29th *]][[*scene: walking down the road *]]
	He was just humming to himself as he walked now. It was cute. I smiled at him and he smiled up at me. He really was the size of a dog, and brown. It was too bad he wasn’t furry. [[*desc: revival, size of a dog, brown. Head piece *]]
	I said, “It’s almost time for my call.”
	He asked, with uncanny understanding, “Okay. Do you want to stop walking when they call?”
	“Yes. If that’s okay with you.”
	“Okay, with me?” He smiled, “Sure it’s okay with me!”
  
	[[*subscene: called from home *]]
	I heard the buzz
  
Outline File Contents #1:
-= Chapter: 12 (1.12) =-
botday: still 29th
walking down the road
desc: revival, size of a dog, brown. Head piece
	called from home
	...
  
-Example of text #2
	He sighed happily as he said, “It is nice to appreciate the little things, even with electronic eyes.”
	[[*time: night time *]]
	[[*botday: 29th day *]]
	“It’s all I have… now. This is now the twenty-ninth day I’ve been a robot-girl.”
	He sighed, “I’m sorry for you. Twenty-ninth? So this is another day, back home?”
	I nodded.
  
Outline File Contents #2:
time: night time
botday: 29th day
	...
  
```

#GUI of KQF2:
![Start screen](screenshots/kqf2%201%20start.png?raw=true&s=200 "First screen" )
![Start screen](screenshots/kqf2%202%20start.png?raw=true&s=200 "Filled in screen" )
![Start screen](screenshots/kqf2%203%20view.png?raw=true&s=200 "View of data screen" )
![Start screen](screenshots/kqf2%204%20morefiles%201.png?raw=true&s=200 "Morefiles data screen 1" )
![Start screen](screenshots/kqf2%204%20morefiles%202.png?raw=true&s=200 "Morefiles data screen 2" )

