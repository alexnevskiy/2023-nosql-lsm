package ru.vk.itmo.kobyzhevaleksandr;

import ru.vk.itmo.Entry;

import java.lang.foreign.MemorySegment;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class MergePeekIterator implements PeekIterator<Entry<MemorySegment>> {

    private final PeekIterator<Entry<MemorySegment>> priorityIterator;
    private final PeekIterator<Entry<MemorySegment>> nonPriorityIterator;
    private final Comparator<MemorySegment> memorySegmentComparator;
    private boolean isPeeked;
    private Entry<MemorySegment> peekedEntry;

    public MergePeekIterator(PeekIterator<Entry<MemorySegment>> priorityIterator,
                             PeekIterator<Entry<MemorySegment>> nonPriorityIterator,
                             Comparator<MemorySegment> memorySegmentComparator) {
        this.priorityIterator = priorityIterator;
        this.nonPriorityIterator = nonPriorityIterator;
        this.memorySegmentComparator = memorySegmentComparator;
    }

    @Override
    public boolean hasNext() {
        return isPeeked || priorityIterator.hasNext() || nonPriorityIterator.hasNext();
    }

    @Override
    public Entry<MemorySegment> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!priorityIterator.hasNext()) {
            return nonPriorityIterator.next();
        }
        if (!nonPriorityIterator.hasNext()) {
            return priorityIterator.next();
        }

        return getNextEntry();
    }

    @Override
    public Entry<MemorySegment> peek() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!isPeeked) {
            peekedEntry = getNextEntry();
            isPeeked = true;
        }
        return peekedEntry;
    }

    private Entry<MemorySegment> getNextEntry() {
        Entry<MemorySegment> priorityEntry = priorityIterator.peek();
        Entry<MemorySegment> nonPriorityEntry = nonPriorityIterator.peek();

        int result = memorySegmentComparator.compare(priorityEntry.key(), nonPriorityEntry.key());
        if (result < 0) {
            priorityIterator.next();
            return priorityEntry;
        } else if (result > 0) {
            nonPriorityIterator.next();
            return nonPriorityEntry;
        }

        priorityIterator.next();
        nonPriorityIterator.next();
        return priorityEntry;
    }
}
