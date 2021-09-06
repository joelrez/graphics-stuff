# graphics-stuff
In this project, I am teaching myself computer graphics. In essence, I am deriving formulas to handle objects in 3D and render them in a 2D frame in Java. A lot of the code comes from http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html, but I've taken a detour to try and do some of the math-related parts on my own such as finding the depth of the pixels and determining whether a pixel lies in the interior of a triangle.

Below, I've 2 triangles in 3D that intersect with each other, and I can rotate the triangles to see them at different angles.

![image](https://user-images.githubusercontent.com/32008471/130649536-02191ad2-b585-4473-8392-eac1173b9097.png)
![image](https://user-images.githubusercontent.com/32008471/130649968-528bb9f4-040e-4d79-b65b-fa104f068404.png)

Later on in this post, I can possibly add some documentation to explain the mathematics behind this in a formal way. The resource I've used attempts to explain this in their blogpost at http://blog.rogach.org/2015/08/how-to-create-your-own-simple-3d-render.html.

# Problems Solved
* Added algorithm that determines whether the pixel we're considering lies within a triangle and places a pixel in the color of the triangle we're considering.
* Added algorithm to determine the depth of the pixel of the triangle we're considering. This helps us make sure the figure is properly drawn based on the information of the triangles.
* Rotating these triangles based on the centers of them rather than at the origin (top left corner.) Now we find the centroid of the triangles, and then I average the centroids. Then I rotate these triangles about the centroid of both of them. I solve this problem whilst streaming on twitch ( https://www.twitch.tv/videos/1140359183 .) On stream, I get help from bokeh_joe who gave me the idea of averaging the centroids of both triangles. On stream, I show that averaging the centroids of 2 triangles is the same as averaging the vertices of all 6 points of both triangles.

# Issues
* I want to add lighting.
