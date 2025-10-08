package com.ridgebotics.ridgescout.types;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.io.File;
import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class ColabArray {
    private enum Action {
        ADD,
        REMOVE
    }

    private List<Diff> changelog = new ArrayList<>();

    private void addChange(Diff change) {
        this.changelog.add(change);
    }

    private List<Diff> getChangelog() {
        return changelog;
    }

    public void add(String item) {
        Diff diff = new Diff();
        diff.action = Action.ADD;
        diff.content = item;
        diff.time = new Date();
        addChange(diff);
    }

    public void remove(String item) {
        Diff diff = new Diff();
        diff.action = Action.REMOVE;
        diff.content = item;
        diff.time = new Date();
        addChange(diff);
    }

    public void remove(int index) {
        remove(get().get(index));
    }

    private static class Diff {
        public Action action;
        public String content;
        public Date time;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj.getClass() != this.getClass()) {
                return false;
            }

            Diff other = (Diff) obj;

            return other.action == this.action &&
                    other.time.getTime() == this.time.getTime() &&
                    other.content.equals(this.content);
        }
    }



    public byte[] encode() throws ByteBuilder.buildingException{
        ByteBuilder bb = new ByteBuilder();

        for(Diff change : this.changelog){
            bb.addInt(change.action.ordinal());
            bb.addString(change.content);
            bb.addLong(change.time.getTime());
        }


        return bb.build();
    }

    public static ColabArray decode(byte[] bytes) throws BuiltByteParser.byteParsingExeption {
        BuiltByteParser bbp = new BuiltByteParser(bytes);
        List<BuiltByteParser.parsedObject> results = bbp.parse();

        if(results.size() % 3 != 0){
            throw new BuiltByteParser.byteParsingExeption("Wrong amount of elements in ColabArray!");
        }

        ColabArray arr = new ColabArray();

        for(int i = 0; i < results.size(); i += 3) {
            Diff diff = new Diff();
            diff.action = Action.values()[(int) results.get(i).get()];
            diff.content = (String) results.get(i+1).get();
            diff.time = new Date((long) results.get(i+2).get());
            arr.addChange(diff);
        }


        return arr;
    }

    public void append(ColabArray other) {

        List<Diff> otherlog = other.getChangelog();

        otherlog.removeIf(diff ->
            this.changelog.contains(diff)
        );

        this.changelog.addAll(otherlog);
        this.changelog = Arrays.asList(sort(this.changelog));
    }

    public void append(File other) {
        byte[] bytes = FileEditor.readFile(other);
        if(bytes == null) return;
        try {
            append(decode(bytes));
        } catch (BuiltByteParser.byteParsingExeption e) {
            AlertManager.error("Failed to append ColabArray!", e);
        }
    }

    private static Diff[] sort(List<Diff> changelog) {
        Diff[] sorted = changelog.toArray(new Diff[0]);

        try {
            Arrays.sort(sorted, (o1, o2) -> (int) (o1.time.getTime() - o2.time.getTime()));
        } catch (Exception e){
            AlertManager.error(e);
        }

        return sorted;
    }

    public List<String> get() {
        List<String> result = new ArrayList<>();

        for(Diff change : changelog) {
            switch (change.action) {
                case ADD:
                    result.add(change.content);
                    break;
                case REMOVE:
                    result.remove(change.content);
                    break;
            }
        }

        return result;
    }

    public boolean contains(String item) {
//        Diff[] sorted = sort();

        for(int i = changelog.size()-1; i >= 0; i--) {
            Diff change = changelog.get(i);
            if(!change.content.equals(item)) continue;
            return change.action == Action.ADD;
        }

        return false;
    }

    public int size() {
        return get().size();
    }
}
