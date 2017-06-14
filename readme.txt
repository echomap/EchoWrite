-=KindleQuickFormatter=-

If command line run Like:

set BOOKPATH=%WRITEPATH%\TheUnderU\Mer
set FILE1=mer1

set BASEP=C:\workspace\repos\acerkitten
set KQFPATH=%BASEP%\wprogs\KindleQuickFormatter\trunk
set BOOKIN1=%BOOKPATH%/%FILE1%.txt
set BOOKNAME1A=%TITLE1%
set BOOKNAME1B=%SUBTITLE1%
set BOOKOUT1_SIG=%BOOKPATH%/ebook/sigil/src1/%FILE1%.html
set BOOKOUT1_SIGDIR=%BOOKPATH%/ebook/sigil/src1/chapters/

java -jar %KQFPATH%/dist/KindleQuickFormatter.jar -inputfile %BOOKIN1% -outputfile %BOOKOUT1_SIG% -storytitle1 %BOOKNAME1A% -storytitle2 %BOOKNAME1B% -formatmode "Sigil"  -sectiondivider -= -removediv -writechapters "%BOOKOUT1_SIGDIR%"

