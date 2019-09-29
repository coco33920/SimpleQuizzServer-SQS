package fr.colin.stfc.objects;

public class RequestFetchQuizzs {

    private Long start;
    private Long end;
    private Long interval;

    public RequestFetchQuizzs(Long start, Long end, Long interval) {
        this.start = start;
        this.end = end;
        this.interval = interval;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public Long getInterval() {
        return interval;
    }
}
