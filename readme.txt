
** Example Text **
```
[[*meta: title: My TItle*]]
[[*meta: series: Series title *]]
[[*meta: volume: 1 *]]
[[*meta: keywords: test,temp, example *]]
[[*meta: @listtimedate: timemark.  *]]
[[*meta: @bookstarttimedata=marker: 0 *]]
-= Section 00: IDEA =-
[[*meta: working title*]]
[[*planning: IDEA;
(==)
idea 1
idea 2
idea 3
(==)
*]]
-= Section 01: First Section =-

-= Chapter 01: First Chapter =-

	[[*timemark: day: 0. time: 0000. marker: 0. text: first morning *]]
	[[*scene: Firt scene *]]
	[[*char: @name=MC @desc=eyes; xxx. mouth; xxx. kind person who is likely to do... things *]]
	story text story text story text story text story text story text
	story text story text story text story text story text story text

	[[*timemark: day: 1. time: 1300. marker: 1. text: afternoon *]]
	[[*scene: Second scene *]]
	[[*loc: location 1 *]]
	[[*char: @name=mc @desc=knowledge; knows nothing *]]
	story text story text story text story text story text story text
	story text story text story text story text story text story text
	story text story text story text story text story text story text
	story text story text story text story text story text story text

	[[*subscene: subscene 1 *]]
	[[*inv: @item=thing1 @char=mc @count=1 @desc=is a thing1 @slot=clothing @status=has @loc=body *]]
	[[*char: @char=mc @desc=another thing about character *]]
	[[*inv: @item=nails @char=mc @count=1 @desc=1/2 cm @count=1 @slot=body*]]
	story text story text story text story text story text story text
	story text story text story text story text story text story text
	story text story text story text story text story text story text
	story text story text story text story text story text story text

```


**3rd iteration**


[[*char: @name=Ted @desc=A ted like person *]]
[[*inv: @name=Ted @count=1 @item=Guitar @desc=a guitar *]]


Little Gui
Able to run quickly for testing, cmd?

Gui
Counter-> Quick spawn filelooper
Outliner, timeline, etc,
-> Parsing Spawn Filelooper
db of doctag data
time markers/scene/section/chapter info


DocTag (all incl section/chapters)
	name
	value
	*time
	
DocTagSub
	name
	value
