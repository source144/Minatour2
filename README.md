# Minatour Assignment

## Implementation
### Minatour Presents (`MinatourPresents.java`)
We'll use the concurrent Linked List (LL) according to the book.\
Minatour has 4 servants (threads) that randomly alternate tasks.\

The tasks are:
1) Add guest present to LL (by id-tag)
2) Remove present from LL (write "Thank You")
3) Randomly check if present is in LL (by id-tag)

#### Specific Implementation
We will have a Concurrent Deque (stack) that will represent our "unordered bag", which is essentially the range from [1, n] presents, where [1, n] is the identifying `tag` and `n` is the
specified number of presents (default `500,000`) and can be set with the `--numPresents` command line argument.

Task (1) will take a present from the "unordered bag" (the stack)
and add it to our implemented Linked List chain in the correct
position.

Task (2) will remove a present (write "Thank You") from the chain

Task (3) will search for a random present (by tag) in the chain.
~~I might implement an IO operation for this task and ask the user for a specific tag to look for, otherwise it will be random number. If I implement it, it would be an optional argument `--io`.~~

#### Concurrent Linked List
We will use a slightly modified version of the **Lock Free List** that is discussed in the book.\
In this implementation we need to easily remove elements concurrently without knowing their `tag` key. It is possibly to guess the `key`s, but out of `500,000` guessing all of them seems counterintuitive. For that purpose I implemented a `pop` method that *removes* and *returns* the first unmarked `tag` it finds in the list. It is done by simple traversal to find the first valid `key`, and then calling `remove` to remove that `key`. Should be noted that the `remove` call might fail, and in that case we try again just like with `find`ing a `Window`. There is also a flag (`Integer.MaxValue`) that indicates a failed `pop` because the list is empty (only dummy head and tail).

#### Remarks
The code is pretty effecient, and running it on 5,000,000 presents takes a about **2 seconds** to complete (even on my old and slow laptop). Enabling output (debug) takes a bit more time.


### Mars Rover
We'll use **_two_** thread types for this particular problem, each responsible for different task and scope.
1) Sensor Thread
2) Report Thread

The sensor threads store data into shared memory on a set interval (`SENSOR_TIME`).
The Report thread compile that data every predifined interval of time (`REPORT_TIME`).

The implementation of the shared data is simply an matrix where each row is for each sensor, and each column is for a time entry of the sensor. I.e. they "y"-axis is the sensors, and the "x"-axis is the time. Each cell is the reading for a specific time by a specific sensor.

Either way, each sensor only has access to its row and the only other access that is done is by the report thread, only to clone the states into two vectors of temp by time. One vector is for `maxTemp`, the other is for `minTemp`. From there, everything necessary for the report can be derived.

## Running the Programs
### Command Line Arguments
Both programs (P1 and P2) are pretty modifiable and you can play around with the parameters through the commandline interface or VS Code or any other code editor/IDE you'd like.

**`MarsRover.java`** configuration:
```js
Available arguments:
   -sensor {int}  -   the sensor interval in ms      (default: 500)
   -report {int}  -   the report interval in ms      (default: 15,000)
   -change {int}  -   the change interval in ms      (default: 5,000)
```

and

**`MinatourPresnts.java`** configuration:
```js
Available arguments:
    -numPresnts {int}  -   number of presents to process      (default: 500,000)
    --debug            -   detail tasks performed by servants (default: off)
```

These are all optional and you can just run the programs without the arguments and the default values will automatically be configured. _Note:_ there are some safeguards and validation this time, but still it's not extremely thorough. Please avoid using any absurd values (larger than a Java integer) as the program may crash while trying to parse that number into an integer.

`MinatourPresnts` example for setting the number of presents to `3000000` and enabling the debug option to see what each *Servant* is up to:
```
javac MinatourPresnts.java
java MinatourPresnts -numPresnts 3000000 --debug
```

`MarsRover` example with an interval of `501`ms for `sensor`s and `15000`ms for `report`s, which don't synchronize but, but the program ensures the match and will change the values appropriately:
```
javac MarsRover.java
java MarsRover -sensor 501 -report 15000
```

### Compiling and Running
Run as you would any other java file.\
For example, execute the `MinatourPresnts.java` file
```
javac MinatourPresnts.java
java MinatourPresnts
```

It is actaully pretty easy to run through VS Code, I even included my `launch.json` settings so you can already ave a few arguments set by default.