import java.awt.Color;
import java.util.Random;
import tester.Tester;
import javalib.funworld.*;
import javalib.worldimages.*;
import javalib.funworld.WorldScene;

//represents a game of Mastermind
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
  ILoMatches listOfMatches;


  //Initializes the game by creating a Random object for generating random sequences of colors
  Mastermind() {
    this.rand = new Random();
  }

  // represents the settings for a game of Mastermind
  Mastermind(boolean duplicatesAllowed, int lengthOfSequence, int numberOfGuesses, ILoColor possibleColors, Random random) {

    // makes sure that the length of the sequence to be guessed is always greater than 0
    if (lengthOfSequence <= 0)  {
      throw new IllegalArgumentException("length guesses must be greater than 0!");
    }

    // makes sure that the number of guesses the player has is greater than 0 
    if (numberOfGuesses <= 0) {
      throw new IllegalArgumentException("number of guesses must be greater than 0!");
    }

    // makes sure that the possible colors the player has to guess from is greater than 0 
    if (possibleColors.length() <= 0) {
      throw new IllegalArgumentException("sequence of colors must be greater than 0!");
    }

    // makes sure that if no duplicates are allowed, then the number of possible colors is greater than the 
    // sequence length
    if (!duplicatesAllowed && (possibleColors.length() < lengthOfSequence)) {
      throw new IllegalArgumentException("sequence of colors must not be greater than the amount of possible colors!");
    }

    // creates a random integer
    this.rand = new Random();

    // modifies the game so that duplicate colors can be allowed in the correct sequence or not
    this.duplicatesAllowed = duplicatesAllowed;

    // modifies the games so that the sequence to be guessed is the length as the given number
    this.lengthOfSequence = lengthOfSequence;

    // modifies the game so that the given number is the amount of guesses the player will get
    this.numberOfGuesses = numberOfGuesses;

    // a list of the possible colors that can be in the correct sequence
    this.possibleColors = possibleColors;

    // the current number of guesses the player has made
    this.currentNumberOfGuesses = 1;

    // the current number of exact matches in the guess
    this.exactMatchesCount = 0;

    // the current number of inexact matches in the guess
    this.inexactMatchesCount = 0;

    this.listOfMatches = new MtLoMatches();


    // the correct sequence to be guessed
    this.correctSequence = possibleColors.generateRandomSequence(rand, duplicatesAllowed, lengthOfSequence);

    // the current list of guesses the player has made so far
    this.listOfGuesses = this.rowsOfGuesses(numberOfGuesses, lengthOfSequence);

  }




  // creates multiple rows of blank guesses given the amount of guesses a player gets
  public ILoLoColor rowsOfGuesses(int rows, int guesses) {
    if (rows <= 0) {
      return new MtLoLoColor();  
    }
    else {
      return new ConsLoLoColor(this.createBlankGuesses(guesses), this.rowsOfGuesses(rows - 1, guesses));
    }
  }


  // creates a list of blank white guesses
  public ILoColor createBlankGuesses(int guesses) {
    if (guesses <= 0) {
      return new MtLoColor();  
    }
    else {
      return new ConsLoColor(Color.WHITE, this.createBlankGuesses(guesses - 1));
    }
  }


  // counts the current number of guesses 
  public int countNumberOfGuesses() {
    if (this.listOfGuesses.isRowFull(this.currentNumberOfGuesses)) {
      return this.currentNumberOfGuesses + 1;

    }
    else {
      return this.currentNumberOfGuesses;
    }
  }



  // handles key events for the game

  // places a color guess based on what number key is pressed
  public World onKeyEvent(String key) {
    // places a color guess based on what number key is pressed
    if ("123456789".contains(key)) {
      Color selectedColor = this.possibleColors.getColorAtIndex(Integer.valueOf(key) - 1);

      // if duplicates are allowed, place the guess
      if (this.duplicatesAllowed) {
        this.listOfGuesses = this.listOfGuesses.placeGuess(this.currentNumberOfGuesses, selectedColor);
      }
      // if duplicates aren't allowed, check if the color is already in the current row
      else if (!this.listOfGuesses.inListAtRow(currentNumberOfGuesses, selectedColor)) {
        this.listOfGuesses = this.listOfGuesses.placeGuess(this.currentNumberOfGuesses, selectedColor);
      }
      // if the color is already in the row, do nothing
    }

    // if enter key is pressed and the row has all guesses placed, submit the current row of guesses
    else if (key.equals("enter") && this.listOfGuesses.isRowFull(currentNumberOfGuesses)) { 

      ILoColor currentGuess = this.listOfGuesses.getRowAtIndex(currentNumberOfGuesses);  

      // returns the exact matches
      this.exactMatchesCount = currentGuess.exactMatches(this.correctSequence);
      // returns the inexact matches
      this.inexactMatchesCount = currentGuess.inexactMatches(this.correctSequence);

      this.listOfMatches = new ConsLoMatches(new Matches(exactMatchesCount, inexactMatchesCount), listOfMatches);

      // checks if the current guess matches the correct sequence exactly, and if it does then game is won
      if (currentGuess.exactMatches(this.correctSequence) == this.lengthOfSequence) {
        // End the world if the guess is correct
        return this.endOfWorld("yay you won! :)");
      }

      if (this.numberOfGuesses == this.currentNumberOfGuesses) {
        return this.endOfWorld("aw you lost! :(");
      }

      // if no match, increment number of guesses to move on to next row
      this.currentNumberOfGuesses += 1;
    }
    // when backspace is pressed, delete the most recently placed guess
    else if (key.equals("backspace")) {
      this.listOfGuesses = this.listOfGuesses.removeLastColor(currentNumberOfGuesses);
    }

    return this;  
  }


  // creates the world scene
  public WorldScene makeScene() {
    int XPosition = ((500 - (20 * this.possibleColors.length())) / 2);
    int YPosition = (500 - (currentNumberOfGuesses * 20));
    WorldImage matches = new TextImage("exact    " + "inexact", 20, Color.BLACK);
    return 
        new WorldScene(500, 700)
        .placeImageXY(new RectangleImage(500, 700, OutlineMode.SOLID, Color.PINK), 250, 350)
        .placeImageXY(matches, 400, 500)
        .placeImageXY(this.listOfGuesses.drawLoLoColor(), XPosition, 400)
        .placeImageXY(this.possibleColors.drawCircles(), XPosition, 650)
        .placeImageXY(listOfMatches.drawLoMatches(), 400, YPosition);
  }

  // creates the last scene of the game depending on if the player wins or loses
  public WorldScene lastScene(String msg) {
    // Create the base scene
    int XPosition = ((500 - (20 * this.possibleColors.length())) / 2);
    int YPosition = (500 - (currentNumberOfGuesses * 20));
    WorldImage matches = new TextImage("exact    " + "inexact", 20, Color.BLACK);
    WorldScene scene = new WorldScene(500, 700);
    return
        // Place the guesses so far on the scene
        scene.placeImageXY(new RectangleImage(500, 700, OutlineMode.SOLID, Color.PINK), 250, 350)
        .placeImageXY(this.listOfGuesses.drawLoLoColor(), XPosition, 400)
        .placeImageXY(matches, 400, 500)
        .placeImageXY(listOfMatches.drawLoMatches(), 400, YPosition)

        // Display the correct sequence
        .placeImageXY(this.correctSequence.drawCircles(), XPosition, 100)

        .placeImageXY(this.possibleColors.drawCircles(), XPosition, 650)

        // Display the end message
        .placeImageXY(new TextImage(msg, 40, Color.BLACK), XPosition, 150);
  }

  /* TEMPLATE:
   * FIELDS:
   *  this.rand... Random
   *  this.duplicatesAllowed... boolean
   *  this.lengthOfSequence... int
   *  this.numberOfGuesses... int
   *  this.possibleColors... ILoColor
   *  this.listOfGuesses... ILoLoColor
   *  this.correctSequence... ILoColor
   *  this.currentNumberOfGuesses... int
   *  this.exactMatchesCount... int
   *  this.inexactMatchesCount... int
   *  this.listOfMatches... ILoMatches
   * METHODS:
   *  this.rowsOfGuesses(int, int)... ILoLoColor
   *  this.createBlankGuesses(int)... ILoColor
   *  this.countNumberOfGuesses()... int
   *  this.onKeyEvent(String)... World
   *  this.makeScene()... WorldScene
   *  this.lastScene(String)... WorldScene 
   * METHODS OF FIELDS:
   */
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

  // places the given color in the list 
  ILoColor placeColorInRow(Color color);

  // removes the last color from the list
  ILoColor removeLastColor();

  // checks if a list contains all white colors
  boolean whiteRow();

  // Generates a random sequence of colors based on the specified parameters
  ILoColor generateRandomSequence(Random rand, boolean duplicatesAllowed, int length);
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

  // returns the inexact matches 
  public int inexactMatches(ILoColor sequence) {
    return 0;
  }

  // checks if the given list length doesnt equal this list
  public boolean inequalLengths(ILoColor sequence) {
    return false;
  }

  // checks if the given color is in this list
  public boolean inList(Color color) {
    return false;
  }

  // removes the first instance of the given color from this list 
  public ILoColor removeColor(Color color) {
    return this;
  }

  // helper for inexact matches
  public int inexactHelper(ILoColor sequence) {
    return 0;
  }

  // draws the list of colors as circles
  public WorldImage drawCircles() {
    return new EmptyImage();
  }

  // returns the color from the given index
  public Color getColorAtIndex(int index) {
    return Color.WHITE;
  }

  // creates the amount of blank guesses given the number of guesses given
  public ILoColor createBlankGuesses(int guesses) {
    return this;
  }

  // places the given color in the list of colors
  public ILoColor placeColorInRow(Color color) {
    return this;
  }

  // removes the last color from the list 
  public ILoColor removeLastColor() {
    return this;
  }

  // checks if the whole row is all white
  public boolean whiteRow() {
    return true;
  }

  // Generates a random sequence of colors based on the specified parameters
  public ILoColor generateRandomSequence(Random rand, boolean duplicatesAllowed, int length) {
    return this;
  }

  /* TEMPLATE:
   * FIELDS:
   * METHODS:
   *  this.duplicates()... boolean
   *  this.exactMatches(ILoColor)... int
   *  this.inexactMatches(ILoColor)... int
   *  this.matchesHelper(Color, ILoColor)... int
   *  this.inexactHelper(ILoColor)... int
   *  this.length()... int
   *  this.inequalLengths(ILoColor)... boolean
   *  this.inList(color)... boolean
   *  this.removeColor(Color)... ILoColor
   *  this.drawCircles()... WorldImage
   *  this.getColorAtIndex(int)... Color
   *  this.createBlankGuesses(int)... ILoColor
   *  this.placeColorInRow(Color)... ILoColor
   *  this.removeLastColor()... ILoColor
   *  this.whiteRow()... boolean
   *  this.generateRandomSequence(Random, boolean, int)... ILoColor
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



  // helper for inexactMatches method
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

  // returns the color in the list at the given index
  public Color getColorAtIndex(int index) {
    if (index == 0) {
      return this.first;
    }
    else return (this.rest.getColorAtIndex(index - 1));
  }


  // creates blank guesses from the number of guesses a player gets
  public ILoColor createBlankGuesses(int guesses) {
    return new MtLoColor();
  }

  // adds the given color to the list of colors
  public ILoColor placeColorInRow(Color color) {
    if (this.first.equals(Color.WHITE)) {
      return new ConsLoColor(color, this.rest);
    }
    else return new ConsLoColor(this.first, this.rest.placeColorInRow(color));
  }


  // removes the last color guessed 
  public ILoColor removeLastColor() {
    // checks if the first color is blank and if it is, recurses on the rest
    if (this.first.equals(Color.WHITE)) {
      return new ConsLoColor(this.first, this.rest.removeLastColor());
    } 
    // if the rest of the row is white, replace the current color with white
    else if (this.rest.whiteRow()) {
      return new ConsLoColor(Color.WHITE, this.rest);
    } 
    else {
      return new ConsLoColor(this.first, this.rest.removeLastColor());
    }
  }


  // checks if the whole list is white 
  public boolean whiteRow() {
    return this.first.equals(Color.WHITE) && this.rest.whiteRow();
  }

  //Generates a random sequence of colors
  public ILoColor generateRandomSequence(Random rand, boolean duplicatesAllowed, int length) {
    // Base case: if the length reaches 0, return an empty sequence
    if (length <= 0) {
      return new MtLoColor();
    }

    // Generate a random index for selecting a color
    int randomIndex = rand.nextInt(this.length());

    // Get the random color from the list
    Color randomColor = this.getColorAtIndex(randomIndex);

    // If duplicates are allowed, keep adding the random color
    if (duplicatesAllowed && length <= 0) {
      return new ConsLoColor(randomColor, this.generateRandomSequence(rand, duplicatesAllowed, length - 1));
    } 
    // If duplicates are not allowed, remove the color from the list and continue
    else {
      return new ConsLoColor(randomColor, this.removeColor(randomColor).generateRandomSequence(rand, false, length - 1));
    }
  }
  /* TEMPLATE:
   * FIELDS:
   *  this.first... Color
   *  this.rest... ILoColor
   * METHODS:
   *  this.duplicates()... boolean
   *  this.exactMatches(ILoColor)... int
   *  this.inexactMatches(ILoColor)... int
   *  this.matchesHelper(Color, ILoColor)... int
   *  this.inexactHelper(ILoColor)... int
   *  this.length()... int
   *  this.inequalLengths(ILoColor)... boolean
   *  this.inList(color)... boolean
   *  this.removeColor(Color)... ILoColor
   *  this.drawCircles()... WorldImage
   *  this.getColorAtIndex(int)... Color
   *  this.createBlankGuesses(int)... ILoColor
   *  this.placeColorInRow(Color)... ILoColor
   *  this.removeLastColor()... ILoColor
   *  this.whiteRow()... boolean
   *  this.generateRandomSequence(Random, boolean, int)... ILoColor
   * METHODS OF FIELDS:
   *  this.first.duplicates()... boolean
   *  this.first.exactMatches(ILoColor)... int
   *  this.first.inexactMatches(ILoColor)... int
   *  this.first.matchesHelper(Color, ILoColor)... int
   *  this.first.inexactHelper(ILoColor)... int
   *  this.first.length()... int
   *  this.first.inequalLengths(ILoColor)... boolean
   *  this.first.inList(color)... boolean
   *  this.first.removeColor(Color)... ILoColor
   *  this.first.drawCircles()... WorldImage
   *  this.first.getColorAtIndex(int)... Color
   *  this.first.createBlankGuesses(int)... ILoColor
   *  this.first.placeColorInRow(Color)... ILoColor
   *  this.first.removeLastColor()... ILoColor
   *  this.first.whiteRow()... boolean
   *  this.first.generateRandomSequence(Random, boolean, int)... ILoColor
   *  this.rest.duplicates()... boolean
   *  this.rest.exactMatches(ILoColor)... int
   *  this.rest.inexactMatches(ILoColor)... int
   *  this.rest.matchesHelper(Color, ILoColor)... int
   *  this.rest.inexactHelper(ILoColor)... int
   *  this.rest.length()... int
   *  this.rest.inequalLengths(ILoColor)... boolean
   *  this.rest.inList(color)... boolean
   *  this.rest.removeColor(Color)... ILoColor
   *  this.rest.drawCircles()... WorldImage
   *  this.rest.getColorAtIndex(int)... Color
   *  this.rest.createBlankGuesses(int)... ILoColor
   *  this.rest.placeColorInRow(Color)... ILoColor
   *  this.rest.removeLastColor()... ILoColor
   *  this.rest.whiteRow()... boolean
   *  this.rest.generateRandomSequence(Random, boolean, int)... ILoColor
   */
}


// represents a List of List of Colors
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

// represents an empty LoLoColor
class MtLoLoColor implements ILoLoColor {

  // Draws this lists of lists of colors; returns empty image for empty list
  public WorldImage drawLoLoColor() {
    return new EmptyImage();
  }

  // Checks if this row is full
  public boolean isRowFull(int rowIndex) {
    return false;
  }

  // Places a guess in the given row;
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    return this;
  }

  // Removes the last color in the given row;
  public ILoLoColor removeLastColor(int rowIndex) {
    return this; 
  }

  // Gets the row at the specified index
  public ILoColor getRowAtIndex(int rowIndex) {
    return new MtLoColor();
  }

  // Checks if the given color is in the specified row
  public boolean inListAtRow(int rowIndex, Color color) {
    return false;
  }
  /* TEMPLATE:
   * FIELDS:
   * METHODS:
   *  this.drawLoLoColor(int, int)... WorldImage
   *  this.isRowFull(int)... boolean
   *  this.placeGuess(int, Color)... ILoLoColor
   *  this.removeLastColor(int)... ILoLoColor
   *  this.getRowAtIndex(int)... ILoColor
   *  this.inListAtRow(int, Color)... boolean
   * METHODS OF FIELDS:
   */
}

// represents a non empty LoLoColor
class ConsLoLoColor implements ILoLoColor {
  ILoColor first;
  ILoLoColor rest;

  // the constructor
  ConsLoLoColor(ILoColor first, ILoLoColor rest) {
    this.first = first;
    this.rest = rest;
  }


  // Draws the lists of lists of colors, stacking the first row above the rest
  public WorldImage drawLoLoColor() {
    return new AboveImage(this.rest.drawLoLoColor(), this.first.drawCircles());
  }


  //Checks if the specified row is full by checking for white colors
  public boolean isRowFull(int rowIndex) {
    if (rowIndex == 1) {
      return !this.first.inList(Color.WHITE);
    }
    else  {
      return this.rest.isRowFull(rowIndex - 1);
    }
  }

  //Places a guess in the specified row, updating that row with the new color
  public ILoLoColor placeGuess(int rowIndex, Color color) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.placeColorInRow(color), this.rest);
    }
    else {
      return new ConsLoLoColor(this.first, this.rest.placeGuess(rowIndex - 1, color));
    }
  }


  //Removes the last color in the specified row
  public ILoLoColor removeLastColor(int rowIndex) {
    if (rowIndex == 1) {
      return new ConsLoLoColor(this.first.removeLastColor(), this.rest);
    } 
    else {
      return new ConsLoLoColor(this.first, this.rest.removeLastColor(rowIndex - 1));
    }
  }

  // Gets the row at the specified index
  public ILoColor getRowAtIndex(int rowIndex) {
    if (rowIndex == 1) {
      return this.first;
    } else {
      return this.rest.getRowAtIndex(rowIndex - 1);
    }
  }


  // Returns true if the given color is in the specified row
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
   *  this.drawLoLoColor(int, int)... WorldImage
   *  this.isRowFull(int)... boolean
   *  this.placeGuess(int, Color)... ILoLoColor
   *  this.removeLastColor(int)... ILoLoColor
   *  this.getRowAtIndex(int)... ILoColor
   *  this.inListAtRow(int, Color)... boolean
   * METHODS OF FIELDS:
   *  this.first.drawLoLoColor(int, int)... WorldImage
   *  this.first.isRowFull(int)... boolean
   *  this.first.placeGuess(int, Color)... ILoLoColor
   *  this.first.removeLastColor(int)... ILoLoColor
   *  this.first.getRowAtIndex(int)... ILoColor
   *  this.first.inListAtRow(int, Color)... boolean
   *  this.rest.drawLoLoColor(int, int)... WorldImage
   *  this.rest.isRowFull(int)... boolean
   *  this.rest.placeGuess(int, Color)... ILoLoColor
   *  this.rest.removeLastColor(int)... ILoLoColor
   *  this.rest.getRowAtIndex(int)... ILoColor
   *  this.rest.inListAtRow(int, Color)... boolean
   */
}

// Represents a match result with counts of exact and inexact matches
class Matches {
  int exact;
  int inexact;

  Matches(int exact, int inexact) {
    this.exact = exact;
    this.inexact = inexact;
  }

  //Creates a visual representation of the match results
  public WorldImage drawMatch() {
    return new TextImage("" + this.exact + "       " + this.inexact, 30, Color.BLACK);
  }
  /* TEMPLATE:
   * FIELDS:
   *  this.exact... int
   *  this.inexact... int
   * METHODS:
   *  this.drawMatch()... WorldImage
   * METHODS OF FIELDS:
   */
}

// represents a list of matches
interface ILoMatches {

  // creates a WorldImage that represents the exact and inexact matches
  WorldImage drawLoMatches();
}

//represents an empty list of matches
class MtLoMatches implements ILoMatches {

  //Draws the visual representation of all matches in the list
  public WorldImage drawLoMatches() {
    return new EmptyImage();
  }
  /* TEMPLATE:
   * FIELDS:
   * METHODS:
   *  this.drawLoMatches()... WorldImage
   * METHODS OF FIELDS:
   */
}

// represents a nonempty list of matches
class ConsLoMatches implements ILoMatches {
  Matches first;
  ILoMatches rest;

  // the constructor
  ConsLoMatches(Matches first, ILoMatches rest) {
    this.first = first;
    this.rest = rest;
  }

  // Draws the first match above the visual representation of the rest of the matches
  public WorldImage drawLoMatches() {
    return new AboveImage(this.first.drawMatch(), this.rest.drawLoMatches());
  }
  /* TEMPLATE:
   * FIELDS:
   *  this.first... Matches
   *  this.rest... ILoMatches
   * METHODS:
   *  this.drawLoMatches()... WorldImage
   * METHODS OF FIELDS:
   */
}



//examples and tests
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

  ILoColor eightColors = new ConsLoColor(green,
      new ConsLoColor(red,
          new ConsLoColor(orange,
              new ConsLoColor(blue,
                  new ConsLoColor(Color.WHITE,
                      new ConsLoColor(Color.YELLOW,
                          new ConsLoColor(Color.GREEN,
                              new ConsLoColor(Color.ORANGE,
                                  new MtLoColor()))))))));

  ILoColor eightColorsv2 = new ConsLoColor(green,
      new ConsLoColor(red,
          new ConsLoColor(orange,
              new ConsLoColor(blue,
                  new ConsLoColor(Color.BLACK,
                      new ConsLoColor(Color.WHITE,
                          new ConsLoColor(Color.YELLOW,
                              new ConsLoColor(Color.GREEN,
                                  new MtLoColor()))))))));

  ILoColor duplicates = new ConsLoColor(green,
      new ConsLoColor(red,
          new ConsLoColor(orange,
              new ConsLoColor(green,
                  new MtLoColor()))));

  ILoLoColor l1 = new MtLoLoColor();
  ILoLoColor l2 = new ConsLoLoColor (this.blueRed, this.l1);
  ILoLoColor l3 = new ConsLoLoColor (this.eightColors, this.l2);

  Matches m1 = new Matches(0, 0);
  Matches m2 = new Matches(3, 1);
  Matches m3 = new Matches(2, 2);
  Matches m4 = new Matches(4, 0);

  ILoMatches m5 = new MtLoMatches();
  ILoMatches m6 = new ConsLoMatches(this.m1, this.m5);
  ILoMatches m7 = new ConsLoMatches(this.m1, new ConsLoMatches(this.m2, this.m5));



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
        && t.checkExpect(this.greenYellowOrangeBlue.inList(orange), true)
        && t.checkExpect(this.mt.inList(Color.magenta), false);
  }

  // testing the inexactMatches method
  boolean testinexactMatches(Tester t) {
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
        t.checkExpect(this.greenRedOrangeBlue.getColorAtIndex(0), this.green)
        && t.checkExpect(this.nineColors.getColorAtIndex(4), Color.BLACK)
        && t.checkExpect(this.mt.getColorAtIndex(8), Color.WHITE)
        && t.checkExpect(this.nineColors.getColorAtIndex(100), Color.WHITE);
  }

  // testing the rowsOfGuesssesMethod
  boolean testRowsOfGuesses(Tester t) {
    return t.checkExpect(game1.rowsOfGuesses(0, 0), new MtLoLoColor())
        && t.checkExpect(game1.rowsOfGuesses(1, 3), new ConsLoLoColor(
            new ConsLoColor(new Color(255, 255, 255),
                new ConsLoColor(new Color(255, 255, 255),
                    new ConsLoColor(new Color(255, 255, 255),
                        new MtLoColor()))),
            new MtLoLoColor()))
        && t.checkExpect(game1.rowsOfGuesses(-1, -1), new MtLoLoColor());
  }

  // testing the length method
  boolean testlength(Tester t) {
    return t.checkExpect(this.mt.length(), 0)
        && t.checkExpect(this.nineColors.length(), 9)
        && t.checkExpect(this.greenRedOrangeBlue.length(), 4);
  }

  // testing the createBlankGuesses method
  boolean testCreateBlankGuesses(Tester t) {
    return t.checkExpect(game1.createBlankGuesses(0), new MtLoColor())
        && t.checkExpect(game1.createBlankGuesses(-5), new MtLoColor())
        && t.checkExpect(game1.createBlankGuesses(1), new ConsLoColor(Color.WHITE, new MtLoColor()))
        && t.checkExpect(game1.createBlankGuesses(5), new ConsLoColor(Color.WHITE,
            new ConsLoColor(Color.WHITE,
                new ConsLoColor(Color.WHITE,
                    new ConsLoColor(Color.WHITE,
                        new ConsLoColor(Color.WHITE, new MtLoColor()))))))
        && t.checkExpect(game1.createBlankGuesses(9), new ConsLoColor(Color.WHITE,
            new ConsLoColor(Color.WHITE,
                new ConsLoColor(Color.WHITE,
                    new ConsLoColor(Color.WHITE,
                        new ConsLoColor(Color.WHITE,
                            new ConsLoColor(Color.WHITE,
                                new ConsLoColor(Color.WHITE,
                                    new ConsLoColor(Color.WHITE,
                                        new ConsLoColor(Color.WHITE, new MtLoColor()))))))))));
  }


  // testing the countNumberOfGuesses method
  boolean testCountNumberOfGuessesNone(Tester t) {
    return t.checkExpect(game1.countNumberOfGuesses(), 1)
        && t.checkExpect(game2.countNumberOfGuesses(), 1)
        && t.checkExpect(game3.countNumberOfGuesses(), 1);
  }


  //testing the onKeyEvent method
  boolean testOnKeyEventEnterKey(Tester t) {
    Mastermind game1 = new Mastermind(false, 4, 10, greenRedOrangeBlue, new Random());

    // Fill the first row with guesses
    game1.onKeyEvent("1");
    game1.onKeyEvent("2");
    game1.onKeyEvent("3");
    game1.onKeyEvent("4");

    // Press "enter" to submit the guess
    game1.onKeyEvent("enter");

    return t.checkExpect(game1.currentNumberOfGuesses, 2); 
  }

  //test pressing enter when the row is incomplete
  boolean testOnKeyEventEnterIncompleteRow(Tester t) {
    Mastermind game1 = new Mastermind(false, 4, 10, greenRedOrangeBlue, new Random());

    // Fill part of the row
    game1.onKeyEvent("1");
    game1.onKeyEvent("2");

    // Press "enter" when the row is incomplete
    game1.onKeyEvent("enter");

    return t.checkExpect(game1.currentNumberOfGuesses, 1); 
  }



  // testing the duplicates method
  boolean testduplicates(Tester t) {
    return t.checkExpect(this.nineColors.duplicates(), false)
        && t.checkExpect(this.mt.duplicates(), false)
        && t.checkExpect(this.duplicates.duplicates(), false);
  } 

  // testing the matchesHelper method
  boolean testmatchesHelper(Tester t) {
    return t.checkExpect(this.nineColors.matchesHelper(red, blueRed), 0)
        && t.checkExpect(this.nineColors.matchesHelper(red, this.nineColors), 0)
        && t.checkExpect(this.nineColors.matchesHelper(green, this.nineColors), 1)
        && t.checkExpect(this.mt.matchesHelper(red, this.nineColors), 0)
        && t.checkExpect(this.mt.matchesHelper(blue, this.mt), 0);
  }


  //testing the inexactHelper method
  boolean testinexactHelper(Tester t) {
    return t.checkExpect(this.nineColors.inexactHelper(this.nineColors), 9)
        && t.checkExpect(this.nineColors.inexactHelper(this.mt), 0)
        && t.checkExpect(this.mt.inexactHelper(this.mt), 0)
        && t.checkExpect(this.greenYellowOrangeBlue.inexactHelper(this.greenRedOrangeBlue), 3);
  }

  //testing the inequalLengths method
  boolean testinequalLengths(Tester t) {
    return t.checkExpect(this.nineColors.inequalLengths(this.nineColors), false)
        && t.checkExpect(this.mt.inequalLengths(blueRed), false)
        && t.checkExpect(this.mt.inequalLengths(this.mt), false)
        && t.checkExpect(this.greenRedOrangeBlue.inequalLengths(blueRed), true)
        && t.checkExpect(this.greenRedOrangeBlue.inequalLengths(this.nineColors), true);
  }

  // testing the removeColor method
  boolean testremoveColor(Tester t) {
    return t.checkExpect(this.blueRed.removeColor(Color.pink), this.blueRed)
        && t.checkExpect(this.nineColors.removeColor(Color.BLACK), this.eightColors)
        && t.checkExpect(this.mt.removeColor(blue), this.mt);
  }

  // testing the whiteRow method
  boolean testwhiteRow(Tester t) {
    return t.checkExpect(this.mt.whiteRow(), true)
        && t.checkExpect(this.eightColors.whiteRow(), false)
        && t.checkExpect(this.blueRed.whiteRow(), false);
  }

  // testing the removeLastColor method 
  boolean testremoveLastColor(Tester t) {
    return t.checkExpect(this.mt.removeLastColor(), this.mt)
        && t.checkExpect(this.nineColors.removeLastColor(), new ConsLoColor(new Color(84, 237, 50),
            new ConsLoColor(new Color(255, 103, 85),
                new ConsLoColor(new Color(255, 156, 18),
                    new ConsLoColor(new Color(112, 207, 245),
                        new ConsLoColor(new Color(0, 0, 0),
                            new ConsLoColor(new Color(255, 255, 255),
                                new ConsLoColor(new Color(255, 255, 0),
                                    new ConsLoColor(new Color(0, 255, 0),
                                        new ConsLoColor(new Color(255, 255, 255), new MtLoColor()))))))))))
        && t.checkExpect(this.blueRedPurple.removeLastColor(), new ConsLoColor(new Color(112, 207, 245),
            new ConsLoColor(new Color(255, 103, 85),
                new ConsLoColor(new Color(255, 255, 255), new MtLoColor()))))
        && t.checkExpect(this.l1.removeLastColor(3), this.l1)
        && t.checkExpect(this.l2.removeLastColor(4), new ConsLoLoColor(
            new ConsLoColor(
                new Color(112, 207, 245), 
                new ConsLoColor(
                    new Color(255, 103, 85), 
                    new MtLoColor() 
                    )
                ),
            new MtLoLoColor() 
            ))
        && t.checkExpect(this.l2.removeLastColor(0), this.l2);
  }


  // testing the isRowFull method
  boolean testrestisRowFull(Tester t) {
    return t.checkExpect(this.l1.isRowFull(0), false)
        && t.checkExpect(this.l1.isRowFull(5), false)
        && t.checkExpect(this.l2.isRowFull(4), false)
        && t.checkExpect(this.l3.isRowFull(0), false);
  }

  // testing the createBlankGuesses method
  boolean testcreateBlankGuesses(Tester t) {
    return t.checkExpect(this.mt.createBlankGuesses(0), this.mt)
        && t.checkExpect(this.mt.createBlankGuesses(4), this.mt)
        && t.checkExpect(this.nineColors.createBlankGuesses(2), new MtLoColor())
        && t.checkExpect(this.nineColors.createBlankGuesses(0), new MtLoColor());   
  }

  // testing the drawLoLoColor method
  boolean testdrawLoLoColor(Tester t) {
    return t.checkExpect(this.l1.drawLoLoColor(), new EmptyImage())
        && t.checkExpect(this.l2.drawLoLoColor(), new AboveImage(
            new EmptyImage(), 
            new BesideImage(  
                new CircleImage(20, OutlineMode.SOLID, new Color(112, 207, 245)),  
                new BesideImage(  
                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 103, 85)), 
                    new EmptyImage())))); 
  }

  // testing the placeGuess method
  boolean testplaceGuess(Tester t) {
    return t.checkExpect(this.l1.placeGuess(0, blue), new MtLoLoColor())
        && t.checkExpect(this.l2.placeGuess(3, orange), new ConsLoLoColor(
            new ConsLoColor(
                new Color(112, 207, 245), 
                new ConsLoColor(
                    new Color(255, 103, 85), 
                    new MtLoColor() 
                    )
                ),
            new MtLoLoColor() 
            ));
  }

  // testing the getRowAtIndex method
  boolean testgetRowAtIndex(Tester t) {
    return t.checkExpect(this.l1.getRowAtIndex(5), new MtLoColor())
        && t.checkExpect(this.l2.getRowAtIndex(1), new ConsLoColor(
            new Color(112, 207, 245), 
            new ConsLoColor(
                new Color(255, 103, 85), 
                new MtLoColor() 
                )
            ))
        && t.checkExpect(this.l3.getRowAtIndex(0), new MtLoColor());
  }

  // testing the drawCircles method
  boolean testdrawCircles(Tester t) {
    return t.checkExpect(this.mt.drawCircles(), new EmptyImage())
        && t.checkExpect(this.blueRed.drawCircles(), new BesideImage(
            new CircleImage(20, OutlineMode.SOLID, new Color(112, 207, 245)), 
            new BesideImage(
                new CircleImage(20, OutlineMode.SOLID, new Color(255, 103, 85)), 
                new EmptyImage() 
                )
            ))
        && t.checkExpect(this.eightColors.drawCircles(), new BesideImage(
            new CircleImage(20, OutlineMode.SOLID, new Color(84, 237, 50)), // First CircleImage
            new BesideImage(
                new CircleImage(20, OutlineMode.SOLID, new Color(255, 103, 85)), // Second CircleImage
                new BesideImage(
                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 156, 18)), // Third CircleImage
                    new BesideImage(
                        new CircleImage(20, OutlineMode.SOLID, new Color(112, 207, 245)), // Fourth CircleImage
                        new BesideImage(
                            new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)), // Fifth CircleImage
                            new BesideImage(
                                new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 0)), // Sixth CircleImage
                                new BesideImage(
                                    new CircleImage(20, OutlineMode.SOLID, new Color(0, 255, 0)), // Seventh CircleImage
                                    new BesideImage(
                                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 200, 0)), // Eighth CircleImage
                                        new EmptyImage() // Final image is EmptyImage
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ));
  }


  // tests for drawMatch method
  boolean testdrawMatch(Tester t) {
    return t.checkExpect(this.m1.drawMatch(), new TextImage("0       0", 30.0, FontStyle.REGULAR, new Color(0, 0, 0)))
        && t.checkExpect(this.m2.drawMatch(), new TextImage("3       1", 30.0, FontStyle.REGULAR, new Color(0, 0, 0)))
        && t.checkExpect(this.m3.drawMatch(), new TextImage("2       2", 30.0, FontStyle.REGULAR, new Color(0, 0, 0)));
  }

  // tests for drawLoMatches method
  boolean testdrawLoMatches(Tester t) {
    return t.checkExpect(this.m5.drawLoMatches(), new EmptyImage())
        && t.checkExpect(this.m6.drawLoMatches(), new AboveImage(new TextImage("0       0", 30.0, FontStyle.REGULAR, new Color(0, 0, 0)), new EmptyImage()));
  }

  // tests for placeColorInRow
  boolean testplaceColorInRow(Tester t) {
    return t.checkExpect(this.mt.placeColorInRow(blue), new MtLoColor())
        && t.checkExpect(this.nineColors.placeColorInRow(red), new ConsLoColor(new Color(84, 237, 50),
            new ConsLoColor(new Color(255, 103, 85),
                new ConsLoColor(new Color(255, 156, 18),
                    new ConsLoColor(new Color(112, 207, 245),
                        new ConsLoColor(new Color(0, 0, 0),
                            new ConsLoColor(new Color(255, 103, 85),
                                new ConsLoColor(new Color(255, 255, 0),
                                    new ConsLoColor(new Color(0, 255, 0),
                                        new ConsLoColor(new Color(255, 200, 0),
                                            new MtLoColor()))))))))))
        && t.checkExpect(this.redBlue.placeColorInRow(orange), new ConsLoColor(new Color(255, 103, 85),
            new ConsLoColor(new Color(112, 207, 245),
                new MtLoColor())));
  }

  // tests for inListAtRow
  boolean testinListAtRow(Tester t) {
    return t.checkExpect(this.l1.inListAtRow(0, blue), false)
        && t.checkExpect(this.l2.inListAtRow(1, blue), true)
        && t.checkExpect(this.l2.inListAtRow(3, red), false)
        && t.checkExpect(this.l3.inListAtRow(1, green), true);
  }

  // tests for generateRandomSequence
  boolean testgenerateRandomSequence(Tester t) {
    Random seedRandom = new Random(42); 

    return t.checkExpect(this.mt.generateRandomSequence(seedRandom, false, 0), new MtLoColor())
        && t.checkExpect(this.redBlue.generateRandomSequence(seedRandom, true, 2), new ConsLoColor(
            new Color(112, 207, 245), 
            new ConsLoColor(
                new Color(255, 103, 85), 
                new MtLoColor() 
                )
            ))
        && t.checkExpect(this.blueRedPurple.generateRandomSequence(seedRandom, false, 0), new MtLoColor());
  }

  // tests for makeScene
  boolean testmakeScene(Tester t) {
    return t.checkExpect(this.game1.makeScene(), new WorldScene(500, 700)
        .placeImageXY(
            new RectangleImage(500, 700, OutlineMode.OUTLINE, new Color(0, 0, 0)),
            250, 350)
        .placeImageXY(
            new RectangleImage(500, 700, OutlineMode.SOLID, new Color(255, 175, 175)),
            250, 350)
        .placeImageXY(
            new TextImage("exact    inexact", 20.0, FontStyle.REGULAR, new Color(0, 0, 0)),
            400, 500)
        .placeImageXY(
            new AboveImage(
                new AboveImage(
                    new AboveImage(
                        new AboveImage(
                            new EmptyImage(),
                            new BesideImage(
                                new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                new BesideImage(
                                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                    new BesideImage(
                                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                        new BesideImage(
                                            new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                            new EmptyImage()))))),
                        new BesideImage(
                            new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                            new BesideImage(
                                new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                new BesideImage(
                                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                    new BesideImage(
                                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                        new EmptyImage()))))),
                    new BesideImage(
                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                        new BesideImage(
                            new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                            new BesideImage(
                                new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                new BesideImage(
                                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                    new EmptyImage()))))),
                new BesideImage(
                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                    new BesideImage(
                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                        new BesideImage(
                            new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                            new BesideImage(
                                new CircleImage(20, OutlineMode.SOLID, new Color(255, 255, 255)),
                                new EmptyImage()))))),
            210, 400)
        .placeImageXY(
            new BesideImage(
                new CircleImage(20, OutlineMode.SOLID, new Color(84, 237, 50)),
                new BesideImage(
                    new CircleImage(20, OutlineMode.SOLID, new Color(255, 103, 85)),
                    new BesideImage(
                        new CircleImage(20, OutlineMode.SOLID, new Color(255, 156, 18)),
                        new BesideImage(
                            new CircleImage(20, OutlineMode.SOLID, new Color(112, 207, 245)),
                            new EmptyImage())))),
            210, 650)
        .placeImageXY(new EmptyImage(), 400, 480));
  }


  Mastermind game1 = new Mastermind(false, 4, 4, greenRedOrangeBlue, new Random());
  Mastermind game2 = new Mastermind(true, 6, 6, nineColors, new Random());
  Mastermind game3 = new Mastermind(true, 7, 3, nineColors, new Random());

  boolean testMastermind(Tester t) {
    int Worldwidth = 500;
    int Worldheight = 700;
    double tick = 1.0;
    return game1.bigBang(Worldwidth, Worldheight, tick);
  }
}