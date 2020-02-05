package bot.command.instruction.concrete;

import bot.Storage;
import bot.Ui;

import bot.command.Command;

import bot.command.instruction.execute.StorageWriting;

import bot.command.instruction.parse.AddTaskInstruction;

import bot.task.Event;
import bot.task.Task;

import java.util.ArrayList;

public class EventInstruction extends AddTaskInstruction
        implements StorageWriting<Task> {

    public EventInstruction(Command... commands) {
        super(commands);
    }

    @Override
    public String getSubCommand() {
        return Event.AT;
    }

    @Override
    public void writeToStore(Storage<Task> store, Ui ui, Task t) {
        store.store(t);
        ui.showTaskStoreMessage(store.getSize());
    }
}
