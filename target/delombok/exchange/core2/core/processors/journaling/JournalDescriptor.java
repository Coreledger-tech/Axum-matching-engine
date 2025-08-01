// Generated by delombok at Thu Nov 14 21:27:17 PST 2024
package exchange.core2.core.processors.journaling;

public class JournalDescriptor {
    private final long timestampNs;
    private final long seqFirst;
    private long seqLast = -1; // -1 if not finished yet
    private final SnapshotDescriptor baseSnapshot;
    private final JournalDescriptor prev; // can be null
    private JournalDescriptor next = null; // can be null

    @java.lang.SuppressWarnings("all")
    public JournalDescriptor(final long timestampNs, final long seqFirst, final SnapshotDescriptor baseSnapshot, final JournalDescriptor prev) {
        this.timestampNs = timestampNs;
        this.seqFirst = seqFirst;
        this.baseSnapshot = baseSnapshot;
        this.prev = prev;
    }

    @java.lang.SuppressWarnings("all")
    public long getTimestampNs() {
        return this.timestampNs;
    }

    @java.lang.SuppressWarnings("all")
    public long getSeqFirst() {
        return this.seqFirst;
    }

    @java.lang.SuppressWarnings("all")
    public long getSeqLast() {
        return this.seqLast;
    }

    @java.lang.SuppressWarnings("all")
    public SnapshotDescriptor getBaseSnapshot() {
        return this.baseSnapshot;
    }

    @java.lang.SuppressWarnings("all")
    public JournalDescriptor getPrev() {
        return this.prev;
    }

    @java.lang.SuppressWarnings("all")
    public JournalDescriptor getNext() {
        return this.next;
    }

    @java.lang.SuppressWarnings("all")
    public void setSeqLast(final long seqLast) {
        this.seqLast = seqLast;
    }

    @java.lang.SuppressWarnings("all")
    public void setNext(final JournalDescriptor next) {
        this.next = next;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof JournalDescriptor)) return false;
        final JournalDescriptor other = (JournalDescriptor) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.getTimestampNs() != other.getTimestampNs()) return false;
        if (this.getSeqFirst() != other.getSeqFirst()) return false;
        if (this.getSeqLast() != other.getSeqLast()) return false;
        final java.lang.Object this$baseSnapshot = this.getBaseSnapshot();
        final java.lang.Object other$baseSnapshot = other.getBaseSnapshot();
        if (this$baseSnapshot == null ? other$baseSnapshot != null : !this$baseSnapshot.equals(other$baseSnapshot)) return false;
        final java.lang.Object this$prev = this.getPrev();
        final java.lang.Object other$prev = other.getPrev();
        if (this$prev == null ? other$prev != null : !this$prev.equals(other$prev)) return false;
        final java.lang.Object this$next = this.getNext();
        final java.lang.Object other$next = other.getNext();
        if (this$next == null ? other$next != null : !this$next.equals(other$next)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof JournalDescriptor;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $timestampNs = this.getTimestampNs();
        result = result * PRIME + (int) ($timestampNs >>> 32 ^ $timestampNs);
        final long $seqFirst = this.getSeqFirst();
        result = result * PRIME + (int) ($seqFirst >>> 32 ^ $seqFirst);
        final long $seqLast = this.getSeqLast();
        result = result * PRIME + (int) ($seqLast >>> 32 ^ $seqLast);
        final java.lang.Object $baseSnapshot = this.getBaseSnapshot();
        result = result * PRIME + ($baseSnapshot == null ? 43 : $baseSnapshot.hashCode());
        final java.lang.Object $prev = this.getPrev();
        result = result * PRIME + ($prev == null ? 43 : $prev.hashCode());
        final java.lang.Object $next = this.getNext();
        result = result * PRIME + ($next == null ? 43 : $next.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "JournalDescriptor(timestampNs=" + this.getTimestampNs() + ", seqFirst=" + this.getSeqFirst() + ", seqLast=" + this.getSeqLast() + ", baseSnapshot=" + this.getBaseSnapshot() + ", prev=" + this.getPrev() + ", next=" + this.getNext() + ")";
    }
}
