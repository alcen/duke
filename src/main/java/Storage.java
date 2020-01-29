import java.util.ArrayList;
import java.util.Scanner;
import java.util.Optional;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class to handle stored items within the bot,
 * and mark them as done. Class Storage also
 * handles file writing and reading
 */
public class Storage {
    private final ArrayList<Task> storedTasks;
    private final String fileDirectory;
    private final String fileName;

    /**
     * Constructs a new Storage, loading saved items
     * from the local file system if possible. If not,
     * an empty Storage is created.
     *
     * @param fd String representing file directory to
     *           load from and store to
     * @param fn String representing file name to be used
     *           in the file directory specified
     */
    public Storage(String fd, String fn) {
        this.fileDirectory = fd;
        this.fileName = fn;
        this.storedTasks = new ArrayList<Task>();
        FileReader toLoadFrom;
        boolean hasReadFile = false;
        try {
            toLoadFrom = new FileReader(Storage.getFullFileAddress(fd, fn));
            hasReadFile = true;
        } catch (FileNotFoundException e) {
            // could not find file in location specified
            // create new empty store
            System.out.println("Could not find local storage");
            toLoadFrom = null;
        }

        if (hasReadFile) {
            Scanner io = new Scanner(toLoadFrom);
            while (io.hasNext()) {
                // main loop to load each saved task
                String typeAndDone = io.nextLine();
                Task currentTask;
                boolean isCompleted;
                if (typeAndDone.startsWith(Deadline.TYPE)) {
                    currentTask = new Deadline(io.nextLine(), io.nextLine());
                    isCompleted = Integer.parseInt(
                        Character.toString(
                            typeAndDone.charAt(
                                Deadline.TYPE.length())
                        )
                    ) == 1;
                } else if (typeAndDone.startsWith(Event.TYPE)) {
                    currentTask = new Event(io.nextLine(), io.nextLine());
                    isCompleted = Integer.parseInt(
                        Character.toString(
                            typeAndDone.charAt(
                                 Event.TYPE.length())
                        )
                    ) == 1;
                } else if (typeAndDone.startsWith(Todo.TYPE)) {
                    currentTask = Todo.makeTodoRaw(io.nextLine());
                    io.nextLine();
                    isCompleted = Integer.parseInt(
                        Character.toString(
                            typeAndDone.charAt(
                                Todo.TYPE.length())
                        )
                    ) == 1;
                } else {
                    // unknown task
                    System.out.println("Unknown task found!");
                    continue;
                }

                if (isCompleted) {
                    currentTask.markAsDone();
                }

                this.storedTasks.add(currentTask);
            }
        }
    }

    /**
     * Adds a String to the stored items
     *
     * @param toStore The Task to be stored
     */
    public void store(Task toStore) {
        this.storedTasks.add(toStore);
    }

    /**
     * Prints out all the stored items,
     * in order which they were stored
     */
    public void printStorage() {
        int length = this.storedTasks.size();
        for (int i = 0; i < length; i++) {
            System.out.println(retrieve(i + 1));
        }
    }

    /**
     * Searches the Storage for a Task with a
     * particular date
     *
     * @param date String representing date
     *
     * @return An Optional containing the Task, if found
     */
    public Optional<ArrayList<Integer>> searchStorage(String date) {
        // parse the String
        PrettyTime pt = new PrettyTime(date);
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        int size = this.storage.size();

        if (pt.hasTime()) {
            for (int i = 0; i < size; i++) {
                if (this.storage.get(i).getPrettyTime().equals(pt)) {
                    indexes.add(i + 1);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (this.storage.get(i).getPrettyTime().matchDate(pt)) {
                    indexes.add(i + 1);
                }
            }
        }

        if (indexes.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(indexes);
        }
    }

    /**
     * Marks a specific item on the list
     * as "done"
     *
     * @param index The index of the item,
     *              as it appears in the list
     */
    public void markAsDone(int index) {
        this.storedTasks.get(index - 1).markAsDone();
    }

    /**
     * Retrieves an entry from the stored
     * items (at index i)
     *
     * @param i Index of the item as it
     *          appears in the list
     * @return A String representing the
     *         item, its position on the list,
     *         and its "done" status
     */
    public String retrieve(int i) {
        return i + ". " + this.storedTasks.get(i - 1);
    }

    /**
     * Gets the number of Tasks currently logged.
     *
     * @return int representing number of Tasks.
     */
    public int getNumTasks() {
        return this.storedTasks.size();
    }

    /**
     * Removes an entry from the stored
     * items (at index i)
     *
     * @param i Index of the item as it appears
     *          in the list
     */
    public void delete(int i) {
        this.storedTasks.remove(i - 1);
    }

    /**
     * Attempts to save the stored items
     * to a file on the local system
     */
    public void saveToDisk() {
        StringBuilder toBeSaved = new StringBuilder();
        for (Task task : storedTasks) {
            // use line breaks to separate the tasks
            toBeSaved.append(task.type())
                    .append(task.isDone() ? "1" : "0")
                    .append("\n")
                    .append(task.getTaskDetails())
                    .append("\n")
                    .append(task.getTaskTime())
                    .append("\n");
        }

        File saveDirectory = new File(this.fileDirectory);
        File saveLocation = new File(
                Storage.getFullFileAddress(this.fileDirectory, this.fileName)
        );
        if (!saveLocation.exists()) {
            try {
                saveDirectory.mkdirs();
                saveLocation.createNewFile();
            } catch (IOException e) {
                // error in creating new file
                System.err.println("IOException1");
                System.err.println(e.getMessage());
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(saveLocation)
            );
            writer.write(toBeSaved.toString());
            writer.close();
        } catch (IOException e) {
            // error in writing to file
            System.err.println("IOException2");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets the full file address relative to the
     * current location
     *
     * @param directory String representing file directory
     * @param name String representing file name
     * @return String representing full file address
     */
    private static String getFullFileAddress(String directory, String name) {
        return directory + "/" + name;
    }
}
