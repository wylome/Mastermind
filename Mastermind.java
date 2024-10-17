
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


  Mastermind() {
    this.rand = new Random();


  }

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




  public ILoColor generateRandomSequence(int length) {
    if (length <= 0) {
      return new MtLoColor();
    } else {
      Color randomColor = this.possibleColors.getColorAtIndex(this.rand.nextInt(this.possibleColors.length()) + 1);
      return new ConsLoColor(randomColor, this.generateRandomSequence(length - 1));
    }

  }



  public ILoLoColor rowsOfGuesses(int rows, int guesses) {
    if (rows <= 0) {
      return new MtLoLoColor();  
    }
    else {
      return new ConsLoLoColor(this.createBlankGuesses(guesses), this.rowsOfGuesses(rows - 1, guesses));
    }
  }


  public ILoColor createBlankGuesses(int guesses) {
    if (guesses <= 0) {
      return new MtLoColor();  
    }
    else {
      return new ConsLoColor(Color.WHITE, this.createBlankGuesses(guesses - 1));
    }
  }


  public int countNumberOfGuesses() {
    if (this.listOfGuesses.isRowFull(this.currentNumberOfGuesses)) {
      return this.currentNumberOfGuesses + 1;

    }
    else {
      return this.currentNumberOfGuesses;
    }
  }


  //&& this.listOfGuesses.inListAtRow(currentNumberOfGuesses, this.possibleColors.getColorAtIndex(Integer.valueOf(key)))) { 
  //  this.listOfGuesses = this.listOfGuesses

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

  // checks if two lists are inequal in length
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

  ILoColor placeColorInRow(Color color);

  ILoColor removeLastColor();

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

  @Override
  public int inexactMatches(ILoColor sequence) {
    return 0;
  }

  @Override
  public boolean inequalLengths(ILoColor sequence) {
    return false;
  }

  @Override
  public boolean inList(Color color) {
    return false;
  }

  @Override
  public ILoColor removeColor(Color color) {
    return this;
  }

  public int inexactHelper(ILoColor sequence) {
    return 0;
  }

  @Override
  public WorldImage drawCircles() {
    return new EmptyImage();
  }

  public Color getColorAtIndex(int index) {
    throw new IllegalArgumentException("No color at this index");
  }

  @Override
  public ILoColor createBlankGuesses(int guesses) {
    return this;
  }

  @Override
  public ILoColor placeColorInRow(Color color) {
    return this;
  }

  @Override
  public ILoColor removeLastColor() {
    return this;
  }

  public boolean whiteRow() {
    return true;
  }
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




  @Override
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

  public Color getColorAtIndex(int index) {
    if (index == 1) {
      return this.first;
    }
    else return (this.rest.getColorAtIndex(index - 1));
  }


  @Override
  public ILoColor createBlankGuesses(int guesses) {
    return new MtLoColor();
  }

  @Override
  public ILoColor placeColorInRow(Color color) {
    if (this.first.equals(Color.WHITE)) {
      return new ConsLoColor(color, this.rest);
    }
    else return new ConsLoColor(this.first, this.rest.placeColorInRow(color));
  }

  @Override
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

  public boolean whiteRow() {
    // Return true if the current color and the rest are white
    return this.first.equals(Color.WHITE) && this.rest.whiteRow();
  }


}

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

class MtLoLoColor implements ILoLoColor {

  @Override
  public WorldImage drawLoLoColor() {
    return new EmptyImage();
  }

  @Override
  public boolean isRowFull(int rowIndex) {
    return false;
  }

  @Override
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    return this;
  }

  public ILoLoColor removeLastColor(int rowIndex) {
    return this; 
  }

  public ILoColor getRowAtIndex(int rowIndex) {
    return new MtLoColor();
  }

  @Override
  public boolean inListAtRow(int rowIndex, Color color) {
    return false;
  }

}

class ConsLoLoColor implements ILoLoColor {
  ILoColor first;
  ILoLoColor rest;

  ConsLoLoColor(ILoColor first, ILoLoColor rest) {
    this.first = first;
    this.rest = rest;
  }

  @Override
  public WorldImage drawLoLoColor() {
    return new AboveImage(this.rest.drawLoLoColor(), this.first.drawCircles());
  }




  @Override
  public boolean isRowFull(int rowIndex) {
    if (rowIndex == 1) {
      return !this.first.inList(Color.WHITE);
    }
    else  {
      return this.rest.isRowFull(rowIndex - 1);
    }

  }

  @Override
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.placeColorInRow(color), this.rest);

    }
    else {
      return new ConsLoLoColor(this.first, this.rest.placeGuess(rowIndex - 1, color));
    }
  }

  public ILoLoColor removeLastColor(int rowIndex) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.removeLastColor(), this.rest);
    } 
    else {
      return new ConsLoLoColor(this.first, this.rest.removeLastColor(rowIndex - 1));
    }
  }


  public ILoColor getRowAtIndex(int rowIndex) {
    if (rowIndex == 1) {
      return this.first;
    } else {
      return this.rest.getRowAtIndex(rowIndex - 1);
    }
  }


  @Override
  public boolean inListAtRow(int rowIndex, Color color) {
    if (rowIndex == 1) {
      return this.first.inList(color);

    }
    else {
      return this.rest.inListAtRow(rowIndex - 1, color);
    }
  }

}






class ExamplesMastermind{
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

  boolean testremoveMatches(Tester t) {
    return 
        t.checkExpect(this.blueRed.inexactMatches(redBlue), 2)
        && t.checkExpect(this.purpleRedBlue.inexactMatches(blueRedPurple), 2)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenRedOrangeBlue), 0)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenYellowOrangeBlue), 0);
  }
  boolean testInexactMatches(Tester t) {
    return 
        t.checkExpect(this.blueRed.inexactMatches(redBlue), 2)
        && t.checkExpect(this.purpleRedBlue.inexactMatches(blueRedPurple), 2)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenRedOrangeBlue), 0)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactMatches(greenYellowOrangeBlue), 0);
  }

  boolean testIndex(Tester t) {
    return 
        t.checkExpect(this.blueRed.getColorAtIndex(2), red);

  }


  Mastermind game1 = new Mastermind(false, 3, 5, blueRedPurple);

  boolean testMastermind(Tester t) {
    int Worldwidth = 500;
    int Worldheight = 700;
    double tick = 1.0;
    return game1.bigBang(Worldwidth, Worldheight, tick);


  }



}


