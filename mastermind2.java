
import java.awt.Color;
import java.util.Random;

import tester.Tester;
import javalib.funworld.*;
import javalib.worldimages.*;
import javalib.funworld.WorldScene;


class Mastermind extends World {

  Random rand;
  boolean duplicatesAllowed;
  int lengthOfSequence;
  int numberOfGuesses;
  ILoColor possibleColors;

  ILoLoColor listOfGuesses;
  ILoColor correctSequence;

  int currentNumberOfGuesses;

  int exactMatchesCount;
  int inexactMatchesCount;


  // Initializes the game by creating a Random object for generating random sequences of colors
  Mastermind() {
    this.rand = new Random();
  }

  // represents the settings for a game of Mastermind
  Mastermind(boolean duplicatesAllowed, int lengthOfSequence, int numberOfGuesses, ILoColor possibleColors) {
    if (lengthOfSequence <= 0)  {
      throw new IllegalArgumentException("length guesses must be greater than 0");
    }

    if (numberOfGuesses <= 0) {
      throw new IllegalArgumentException("number of guesses must be greater than 0");
    }
    if (possibleColors.length() <= 0) {
      throw new IllegalArgumentException("sequence of colors must be greater than 0");
    }

    this.rand = new Random();
    this.duplicatesAllowed = duplicatesAllowed;
    this.lengthOfSequence = lengthOfSequence;
    this.numberOfGuesses = numberOfGuesses;
    this.possibleColors = possibleColors;
    this.currentNumberOfGuesses = 1;

    this.exactMatchesCount = 0;
    this.inexactMatchesCount = 0;

    this.correctSequence = this.generateRandomSequence(lengthOfSequence);


    this.listOfGuesses = this.rowsOfGuesses(numberOfGuesses,lengthOfSequence);
  }



  // Generates a random sequence of colors of the specified length, using the possible colors list
  public ILoColor generateRandomSequence(int length) {
    if (length <= 0) {
      return new MtLoColor();
    } else {
      Color randomColor = this.possibleColors.getColorAtIndex(this.rand.nextInt(this.possibleColors.length()) + 1);
      return new ConsLoColor(randomColor, this.generateRandomSequence(length - 1));
    }

  }


  // Creates a list of blank rows for guesses, with the given number of rows and guesses per row
  public ILoLoColor rowsOfGuesses(int rows, int guesses) {
    if (rows <= 0) {
      return new MtLoLoColor();  
    }
    else {
      return new ConsLoLoColor(this.createBlankGuesses(guesses), this.rowsOfGuesses(rows - 1, guesses));
    }
  }

  // Generates a list of white color placeholders representing blank guesses, with the specified number of guesses
  public ILoColor createBlankGuesses(int guesses) {
    if (guesses <= 0) {
      return new MtLoColor();  
    }
    else {
      return new ConsLoColor(Color.WHITE, this.createBlankGuesses(guesses - 1));
    }
  }

  // Counts the number of guesses made so far, incrementing if the current row of guesses is full
  public int countNumberOfGuesses() {
    if (this.listOfGuesses.isRowFull(this.currentNumberOfGuesses)) {
      return this.currentNumberOfGuesses + 1;

    }
    else {
      return this.currentNumberOfGuesses;
    }
  }


  // handles key events for the game
  public World onKeyEvent(String key) {
    // places a color guess based on what number key is pressed
    if ("123456789".contains(key) && this.duplicatesAllowed) {
      this.listOfGuesses = this.listOfGuesses.placeGuess(this.currentNumberOfGuesses, 
          this.possibleColors.getColorAtIndex(Integer.valueOf(key)));

    }

    // if duplicates aren't allowed in the game, whenever a color guess is already in the list, it just doesn't place it
    else if ("123456789".contains(key) 
        && !this.duplicatesAllowed) {
      this.listOfGuesses = this.listOfGuesses.placeGuess(this.currentNumberOfGuesses, 
          this.possibleColors.getColorAtIndex(Integer.valueOf(key)));
    }

    else if ("123456789".contains(key) 
        // duplicates not allowed
        && !this.duplicatesAllowed 
        // if the color you're trying to place is already in the list of guesses, it wont place it
        && this.listOfGuesses.inListAtRow(currentNumberOfGuesses, this.possibleColors.getColorAtIndex(Integer.valueOf(key)))) {
      this.listOfGuesses = this.listOfGuesses.placeGuess(this.currentNumberOfGuesses, 
          Color.WHITE);
    }


    // if enter key is pressed and the row has all guesses placed, submit the current row of guesses
    else if (key.equals("enter") && this.listOfGuesses.isRowFull(currentNumberOfGuesses)) { 
      ILoColor currentGuess = this.listOfGuesses.getRowAtIndex(currentNumberOfGuesses);  // Capture guess

      // returns the exact matches
      this.exactMatchesCount = currentGuess.exactMatches(this.correctSequence);
      // returns the inexact matches
      this.inexactMatchesCount = currentGuess.inexactMatches(this.correctSequence);

      // checks if the current guess matches the correct sequence exactly, and if it does then game is won
      if (currentGuess.exactMatches(this.correctSequence) == this.lengthOfSequence) {
        // End the world if the guess is correct
        return this.endOfWorld("You won! :)");
      }

      if (this.numberOfGuesses == this.currentNumberOfGuesses) {
        return this.endOfWorld("You lost! :(");
      }

      // if no match, just increment number of guesses to move on to the next row
      this.currentNumberOfGuesses += 1;
    }
    // when backspace is pressed, delete the most recently placed guess
    else if (key.equals("backspace")) {
      this.listOfGuesses = this.listOfGuesses.removeLastColor(currentNumberOfGuesses);
    }

    return this;  // return this world if no condition is met
  }

  // Draws the current game scene, including the list of guesses, available colors, and match counts (exact and inexact)
  public WorldScene makeScene() {
    int XPosition = ((500 - (20 * this.possibleColors.length())) / 2);
    return 
        new WorldScene(500, 700)
        .placeImageXY(new RectangleImage(500, 700, OutlineMode.SOLID, Color.PINK), 250, 350)
        .placeImageXY(this.listOfGuesses.drawLoLoColor(), XPosition, 400)
        .placeImageXY(this.possibleColors.drawCircles(), XPosition, 650)
        .placeImageXY(new TextImage("Exact Matches: " + exactMatchesCount, 20, Color.BLACK), 250, 50)
        .placeImageXY(new TextImage("Inexact Matches: " + inexactMatchesCount, 20, Color.BLACK), 250, 80);
  }

  // Displays the final game scene with the correct sequence and a message indicating whether the player won or lost
  public WorldScene lastScene(String msg) {
    // Create the base scene
    int XPosition = ((500 - (20 * this.possibleColors.length())) / 2);

    WorldScene scene = new WorldScene(500, 700);
    return
        // Place the guesses so far on the scene
        scene.placeImageXY(new RectangleImage(500, 700, OutlineMode.SOLID, Color.PINK), 250, 350)
        .placeImageXY(this.listOfGuesses.drawLoLoColor(), XPosition, 400)

        // Display the correct sequence
        .placeImageXY(this.correctSequence.drawCircles(), XPosition, 100)

        .placeImageXY(this.possibleColors.drawCircles(), XPosition, 650)

        // Display the end message
        .placeImageXY(new TextImage(msg, 40, Color.BLACK), XPosition, 150);
  }
}





// represents a list of colors
interface ILoColor {

  // checks if there are duplicate colors in the sequence
  boolean duplicates();

  // returns the number of exact matches in the guessed list of colors
  int exactMatches(ILoColor sequence);

  // returns the number of inexact matches in the guessed list of colors
  int inexactMatches(ILoColor sequence);

  // helper method for exact matches
  int matchesHelper(Color correct, ILoColor rest);

  // helper method for inexact matches
  int inexactHelper(ILoColor sequence);

  // counts the length of this list of colors
  int length();

  // checks if two lists are unequal in length
  boolean inequalLengths(ILoColor sequence);

  // checks if the given color is in this list of colors
  boolean inList(Color color);

  // removes the first instance of a color from this list of colors if found in the list
  ILoColor removeColor(Color color);

  // draws out the list of Colors
  WorldImage drawCircles();

  // returns the color at the given index
  Color getColorAtIndex(int index);

  // creates a list of white circles the same amount of the guesses to be made
  ILoColor createBlankGuesses(int guesses);

  // Places a color in the current row of guesses, replacing the first blank guess (if any)
  ILoColor placeColorInRow(Color color);

  // Removes the last non-white color from the current row of guesses.
  ILoColor removeLastColor();

  // Checks whether all colors in the current row are white (if the row is empty)
  boolean whiteRow();
}



// represents an empty list of colors
class MtLoColor implements ILoColor {

  // checks if there are any duplicates in the list of colors
  public boolean duplicates() {
    return false;
  }

  // returns the length of a list
  public int length() {
    return 0;
  }

  // returns the amount of exact matches, 0 for an empty list
  public int exactMatches(ILoColor sequence) {
    return 0;
  }

  // helper for exact matches, returns 0 for an empty list
  public int matchesHelper(Color correct, ILoColor rest) {
    return 0;

  }

  // returns the number of inexact matches in the guessed list of colors
  public int inexactMatches(ILoColor sequence) {
    return 0;
  }

  // checks if this empty list and the given list are equal
  public boolean inequalLengths(ILoColor sequence) {
    return false;
  }

  // checks if the given color is in this empty list of colors
  public boolean inList(Color color) {
    return false;
  }

  // removes the first instance of a color from this list of empty colors if found in the list
  public ILoColor removeColor(Color color) {
    return this;
  }

  // helper method for inexact matches
  public int inexactHelper(ILoColor sequence) {
    return 0;
  }

  // Returns an empty image placeholder for drawing a list of circles
  public WorldImage drawCircles() {
    return new EmptyImage();
  }

  // Throws an exception indicating that there is no color at the specified index in the list of colors
  public Color getColorAtIndex(int index) {
    throw new IllegalArgumentException("No color at this index");
  }

  // Returns the current instance of the list of colors
  public ILoColor createBlankGuesses(int guesses) {
    return this;
  }

  // Returns the current instance of the list of color
  public ILoColor placeColorInRow(Color color) {
    return this;
  }

  // Returns the current instance of the list of colors 
  public ILoColor removeLastColor() {
    return this;
  }

  // indicates row is white (represents a blank row) 
  public boolean whiteRow() {
    return true;
  }
  /* TEMPLATE:
   * FIELDS:
   * METHODS:
   *  this.duplicates()... boolean
   *  this.length()... int
   *  this.exactMatches(ILoColor)... int
   *  this.matchesHelper(Color, ILoColor)... int
   *  this.inexactMatches(ILoColor)... int
   *  this.inequalLengths(ILoColor)... boolean
   *  this.inList(Color)... boolean
   *  this.removeColor(Color)... ILoColor
   *  this.inexactHelper(ILoColor)... int
   *  this.drawCircles()... WorldImage
   *  this.getColorAtIndex(int)... Color
   *  this.createBlankGuesses(int)... ILoColor
   *  this.placeColorInRow(Color)... ILoColor
   *  this.removeLastColor()... ILoColor
   *  this.whiteRow()... boolean
   * METHODS OF FIELDS:
   */
}


// represents a non empty list of colors
class ConsLoColor implements ILoColor {
  Color first;
  ILoColor rest;
  ConsLoColor(Color first, ILoColor rest) {
    this.first = first;
    this.rest = rest;
  }

  // returns the length of the list
  public int length() {
    return 1 + this.rest.length();

  }


  // checks if there are any duplicates in the list of colors
  public boolean duplicates() {
    return false;
  }


  //helper for exactMatches, adds 1 if two colors are an exact matches, else adds 0 
  public int matchesHelper(Color correct, ILoColor rest) {
    if (this.first.equals(correct)) {
      return 1 + this.rest.exactMatches(rest);
    }
    else {
      return 0 + this.rest.exactMatches(rest);
    }
  }

  // returns the amount of exact matches in the list of colors
  public int exactMatches(ILoColor sequence) {
    return (sequence.matchesHelper(this.first, this.rest));

  }

  // returns the amount of inexact matches
  public int inexactMatches(ILoColor sequence) {
    return this.inexactHelper(sequence) - this.exactMatches(sequence);
  }


  // returns if two lists are inequal in length
  public boolean inequalLengths(ILoColor sequence) {
    return this.length() != sequence.length();
  }

  // checks if the given color is in the list of colors
  public boolean inList(Color color) {
    return (this.first.equals(color)) || this.rest.inList(color);

  }


  // removes the first instance of a color in a list of colors if found
  public ILoColor removeColor(Color color) {
    if (this.first.equals(color)) {
      return this.rest; 
    }
    return new ConsLoColor(this.first, this.rest.removeColor(color));
  }


  // helper method for calculating inexact matches
  public int inexactHelper(ILoColor sequence) {
    if (sequence.inList(this.first)) {
      return 1 + this.rest.inexactHelper(sequence.removeColor(this.first));
    }
    return this.rest.inexactHelper(sequence);
  }


  //draws the circles in the list of colors
  public WorldImage drawCircles() {
    return new BesideImage(new CircleImage(20, OutlineMode.SOLID, this.first), 
        this.rest.drawCircles());
  }

  // Returns the color at the specified index in the list
  public Color getColorAtIndex(int index) {
    if (index == 1) {
      return this.first;
    }
    else return (this.rest.getColorAtIndex(index - 1));
  }


  // Creates a new empty list of colors, which serves as a blank set of guesses
  public ILoColor createBlankGuesses(int guesses) {
    return new MtLoColor();
  }

  // Places a specified color in the first available (white) position in the row of colors
  public ILoColor placeColorInRow(Color color) {
    if (this.first.equals(Color.WHITE)) {
      return new ConsLoColor(color, this.rest);
    }
    else return new ConsLoColor(this.first, this.rest.placeColorInRow(color));
  }

  // Removes the last non-white color from the list of colors
  public ILoColor removeLastColor() {
    // Check if the current color is white
    if (this.first.equals(Color.WHITE)) {
      // If white, keep it and proceed to remove from the rest
      return new ConsLoColor(this.first, this.rest.removeLastColor());
    } 
    // Check if the rest of the list is all white
    else if (this.rest.whiteRow()) {
      // If the rest is all white, replace the current color with white
      return new ConsLoColor(Color.WHITE, this.rest);
    } 
    // Otherwise, recurse into the rest of the list
    else {
      return new ConsLoColor(this.first, this.rest.removeLastColor());
    }
  }

  // Returns true if both the current color and the rest of the list are white, indicating that the entire row is blank or unused
  public boolean whiteRow() {
    // Return true if the current color and the rest are white
    return this.first.equals(Color.WHITE) && this.rest.whiteRow();
  }
  /* TEMPLATE:
   * FIELDS:
   *  this.first... Color
   *  this.rest... ILoColor
   * METHODS:
   *  this.duplicates()... boolean
   *  this.length()... int
   *  this.exactMatches(ILoColor)... int
   *  this.matchesHelper(Color, ILoColor)... int
   *  this.inexactMatches(ILoColor)... int
   *  this.inequalLengths(ILoColor)... boolean
   *  this.inList(Color)... boolean
   *  this.removeColor(Color)... ILoColor
   *  this.inexactHelper(ILoColor)... int
   *  this.drawCircles()... WorldImage
   *  this.getColorAtIndex(int)... Color
   *  this.createBlankGuesses(int)... ILoColor
   *  this.placeColorInRow(Color)... ILoColor
   *  this.removeLastColor()... ILoColor
   *  this.whiteRow()... boolean
   * METHODS OF FIELDS:
   *  this.first.duplicates()... boolean
   *  this.first.length()... int
   *  this.first.exactMatches(ILoColor)... int
   *  this.first.matchesHelper(Color, ILoColor)... int
   *  this.first.inexactMatches(ILoColor)... int
   *  this.first.inequalLengths(ILoColor)... boolean
   *  this.first.inList(Color)... boolean
   *  this.first.removeColor(Color)... ILoColor
   *  this.first.inexactHelper(ILoColor)... int
   *  this.first.drawCircles()... WorldImage
   *  this.first.getColorAtIndex(int)... Color
   *  this.first.createBlankGuesses(int)... ILoColor
   *  this.first.placeColorInRow(Color)... ILoColor
   *  this.first.removeLastColor()... ILoColor
   *  this.first.whiteRow()... boolean
   *  this.rest.duplicates()... boolean
   *  this.rest.length()... int
   *  this.rest.exactMatches(ILoColor)... int
   *  this.rest.matchesHelper(Color, ILoColor)... int
   *  this.rest.inexactMatches(ILoColor)... int
   *  this.rest.inequalLengths(ILoColor)... boolean
   *  this.rest.inList(Color)... boolean
   *  this.rest.removeColor(Color)... ILoColor
   *  this.rest.inexactHelper(ILoColor)... int
   *  this.rest.drawCircles()... WorldImage
   *  this.rest.getColorAtIndex(int)... Color
   *  this.rest.createBlankGuesses(int)... ILoColor
   *  this.rest.placeColorInRow(Color)... ILoColor
   *  this.rest.removeLastColor()... ILoColor
   *  this.rest.whiteRow()... boolean
   */
}


// represents a list of list of colors
interface ILoLoColor {


  // draws the lists of lists of colors
  WorldImage drawLoLoColor();

  // checks if the row at the given index is all guessed
  boolean isRowFull(int rowIndex);

  // places a guess in the given row
  ILoLoColor placeGuess(int rowIndex, Color color);

  // removes the last color in the given row
  ILoLoColor removeLastColor(int rowIndex);

  // gets the row at the given index
  ILoColor getRowAtIndex(int rowIndex);

  // returns if there is the given color in the given row 
  boolean inListAtRow(int rowIndex, Color color);
}


// represents an empty list of list of colors
class MtLoLoColor implements ILoLoColor {

  // Returns an empty image because there are no rows to display.
  public WorldImage drawLoLoColor() {
    return new EmptyImage();
  }

  // Always returns false because there are no rows to check for guesses
  public boolean isRowFull(int rowIndex) {
    return false;
  }

  // Returns this empty list unchanged, as no guesses can be placed in an empty list
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    return this;
  }

  // Returns this empty list unchanged, as there are no colors to remove
  public ILoLoColor removeLastColor(int rowIndex) {
    return this; 
  }

  // Returns an empty row because there are no rows to retrieve
  public ILoColor getRowAtIndex(int rowIndex) {
    return new MtLoColor();
  }

  // Always returns false because there are no rows or colors to check
  public boolean inListAtRow(int rowIndex, Color color) {
    return false;
  }
  /* TEMPLATE:
   * FIELDS:
   * METHODS:
   *  this.drawLoLoColor()... WorldImage
   *  this.isRowFull(int)... boolean
   *  this.placeGuess(int, Color)... ILoLoColor
   *  this.removeLastColor(int).... ILoLoColor
   *  this.getRowAtIndex(int)... ILoColor
   *  this.inListAtRow(int, Color)... boolean
   * METHODS OF FIELDS:
   */
}


// represents a nonempty list of list of colors
class ConsLoLoColor implements ILoLoColor {
  ILoColor first;
  ILoLoColor rest;

  ConsLoLoColor(ILoColor first, ILoLoColor rest) {
    this.first = first;
    this.rest = rest;
  }

  // Draws the list of rows of guesses 
  public WorldImage drawLoLoColor() {
    return new AboveImage(this.rest.drawLoLoColor(), this.first.drawCircles());
  }


  // Checks if the row at the given index is full 
  public boolean isRowFull(int rowIndex) {
    if (rowIndex == 1) {
      return !this.first.inList(Color.WHITE);
    }
    else  {
      return this.rest.isRowFull(rowIndex - 1);
    }
  }

  // Places the given color guess in the row at the specified index and returns an updated list of lists with the new guess added
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.placeColorInRow(color), this.rest);
    }
    else {
      return new ConsLoLoColor(this.first, this.rest.placeGuess(rowIndex - 1, color));
    }
  }

  // Removes the last placed color from the row at the given index and returns an updated list of lists with the last color removed from that row
  public ILoLoColor removeLastColor(int rowIndex) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.removeLastColor(), this.rest);
    } 
    else {
      return new ConsLoLoColor(this.first, this.rest.removeLastColor(rowIndex - 1));
    }
  }

  // Retrieves the row (list of colors) at the given index from the list of rows
  public ILoColor getRowAtIndex(int rowIndex) {
    if (rowIndex == 1) {
      return this.first;
    } 
    else {
      return this.rest.getRowAtIndex(rowIndex - 1);
    }
  }


  // Checks if the given color is present in the row at the specified index within the list of rows
  public boolean inListAtRow(int rowIndex, Color color) {
    if (rowIndex == 1) {
      return this.first.inList(color);
    }
    else {
      return this.rest.inListAtRow(rowIndex - 1, color);
    }
  }
  /* TEMPLATE:
   * FIELDS:
   *  this.first... ILoColor
   *  this.rest... ILoLoColor
   * METHODS:
   *  this.drawLoLoColor()... WorldImage
   *  this.isRowFull(int)... boolean
   *  this.placeGuess(int, Color)... ILoLoColor
   *  this.removeLastColor(int).... ILoLoColor
   *  this.getRowAtIndex(int)... ILoColor
   *  this.inListAtRow(int, Color)... boolean
   * METHODS OF FIELDS:
   *  this.first.drawLoLoColor()... WorldImage
   *  this.first.isRowFull(int)... boolean
   *  this.first.placeGuess(int, Color)... ILoLoColor
   *  this.first.removeLastColor(int).... ILoLoColor
   *  this.first.getRowAtIndex(int)... ILoColor
   *  this.first.inListAtRow(int, Color)... boolean
   *  this.rest.drawLoLoColor()... WorldImage
   *  this.rest.isRowFull(int)... boolean
   *  this.rest.placeGuess(int, Color)... ILoLoColor
   *  this.rest.removeLastColor(int).... ILoLoColor
   *  this.rest.getRowAtIndex(int)... ILoColor
   *  this.rest.inListAtRow(int, Color)... boolean
   */
}





// examples and tests
class ExamplesMastermind {
  Color blue = new Color(112, 207, 245);
  Color red = new Color(255, 103, 85);
  Color purple = new Color(170, 112, 245);
  Color orange = new Color(255, 156, 18);
  Color yellow = new Color(255, 244, 38);
  Color green = new Color(84, 237, 50);


  ILoColor mt = new MtLoColor();
  ILoColor blueRed = new ConsLoColor(blue,
      new ConsLoColor(red,

          new MtLoColor()));

  ILoColor redBlue = new ConsLoColor(red,
      new ConsLoColor(blue,

          new MtLoColor()));

  ILoColor blueRedPurple = new ConsLoColor(blue,
      new ConsLoColor(red,
          new ConsLoColor(purple,
              new MtLoColor())));

  ILoColor purpleRedBlue = new ConsLoColor(purple,
      new ConsLoColor(red,
          new ConsLoColor(blue,
              new MtLoColor())));

  ILoColor greenYellowOrangeBlue =  new ConsLoColor(green,
      new ConsLoColor(yellow,
          new ConsLoColor(orange,
              new ConsLoColor(blue,
                  new MtLoColor()))));
  ILoColor greenRedOrangeBlue =  new ConsLoColor(green,
      new ConsLoColor(red,
          new ConsLoColor(orange,
              new ConsLoColor(blue,
                  new MtLoColor()))));

  ILoColor nineColors = new ConsLoColor(green,
      new ConsLoColor(red,
          new ConsLoColor(orange,
              new ConsLoColor(blue,
                  new ConsLoColor(Color.BLACK,
                      new ConsLoColor(Color.WHITE,
                          new ConsLoColor(Color.YELLOW,
                              new ConsLoColor(Color.GREEN,
                                  new ConsLoColor(Color.ORANGE,
                                      new MtLoColor())))))))));

  WorldScene background = new WorldScene(500, 500);

  // testing the exact matches
  boolean testExactMatches(Tester t) {
    return 
        t.checkExpect(this.blueRed.exactMatches(redBlue), 0)
        && t.checkExpect(this.purpleRedBlue.exactMatches(blueRedPurple), 1)
        && t.checkExpect(this.greenYellowOrangeBlue.exactMatches(greenRedOrangeBlue), 3)
        && t.checkExpect(this.greenYellowOrangeBlue.exactMatches(greenYellowOrangeBlue), 4);
  }

  // testing the inList method
  boolean testinList(Tester t) {
    return 
        t.checkExpect(this.blueRed.inList(green), false)
        && t.checkExpect(this.purpleRedBlue.inList(blue), true)
        && t.checkExpect(this.greenYellowOrangeBlue.inList(green), true)
        && t.checkExpect(this.greenYellowOrangeBlue.inList(orange), true);
  }

  // testing the inexactMatches method
  boolean testremoveMatches(Tester t) {
    return 
        t.checkExpect(this.blueRed.inexactMatches(redBlue), 2)
        && t.checkExpect(this.purpleRedBlue.inexactMatches(blueRedPurple), 2)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenRedOrangeBlue), 0)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenYellowOrangeBlue), 0)
        && t.checkExpect(this.blueRed.inexactMatches(redBlue), 2)
        && t.checkExpect(this.purpleRedBlue.inexactMatches(blueRedPurple), 2)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenRedOrangeBlue), 0)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenYellowOrangeBlue), 0);
  }

  // testing the getColorAtIndex method
  boolean testIndex(Tester t) {
    return 
        t.checkExpect(this.blueRed.getColorAtIndex(2), red)
        && t.checkExpect(this.greenRedOrangeBlue.getColorAtIndex(1), green)
        && t.checkExpect(this.nineColors.getColorAtIndex(4), blue)
        && t.checkException(new IllegalArgumentException("No color at this index"), this.mt, "getColorAtIndex", 4)
        && t.checkException(new IllegalArgumentException("No color at this index"), this.nineColors, "getColorAtIndex", 10);
  }


  Mastermind game1 = new Mastermind(false, 3, 5, blueRedPurple);

  boolean testMastermind(Tester t) {
    int Worldwidth = 500;
    int Worldheight = 700;
    double tick = 1.0;
    return game1.bigBang(Worldwidth, Worldheight, tick);
  }
}

