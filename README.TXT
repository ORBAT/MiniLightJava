This is a Java port of the minimal global illumination renderer MiniLight (http://www.hxa.name/minilight). This version was done by Tom Eklöf (tom.eklof gmail com).

I have strayed from MiniLight's goal of simplicity just for the sake of teaching myself how to do things like threading in Java.


USAGE

java -jar MiniLight.jar [options] modelfile

will produce a .ppm file named modelfile.ppm

Some models are provided in the models/ subdirectory


The currently implemented options are:

Option                                  Description                            
------                                  -----------                            
--help                                  Prints usage information               
--image                                 Alternate name for image file.         
                                          (Defaults to model name + ".ppm")    
--luminance <Float>                     Sets the display luminance to be used  
                                          when saving images. The lower the    
                                          number, the brighter the image.      
                                          (default: 200.0)                     
--override <Integer>                    Overrides the number of iterations     
                                          specified in the model file.         
--period <Integer>                      Set the save period (in seconds).      
                                          Currently only works in single-      
                                          threaded mode. (default: 360)        
--seed <Long>                           Set random seed (default: 42)          
--threads <Integer>                     Number of threads to use. 1 naturally  
                                          means no multithreading, and this is 
                                          the default (default: 1)             

All options can be abbreviated, so --luminance can be written as -l


TROUBLESHOOTING

Exception in thread "main" java.lang.NoClassDefFoundError: joptsimple/OptionException:
- Make sure that the jopt-simple-3.2.jar is either in the lib/ subdirectory or somewhere else in your CLASSPATH

