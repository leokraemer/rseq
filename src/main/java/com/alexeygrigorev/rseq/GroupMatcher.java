package com.alexeygrigorev.rseq;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class GroupMatcher<E> extends ParentMatcher<E> {

    private List<ParentMatcher<E>> matchers;

    GroupMatcher(List<ParentMatcher<E>> matchers) {
        Validate.isTrue(!matchers.isEmpty(), "There should be at least one matcher in the group");
        this.matchers = matchers;
    }

    @Override
    public boolean match(E object) {
        Iterator<ParentMatcher<E>> it = matchers.iterator();
        ParentMatcher<E> matcher = it.next();
        if (!matcher.match(object)) {
            return false;
        }

        Iterator<E> matchIterator = context().getCurrentMatchIterator();

        while (it.hasNext()) {
            matcher = it.next();
            if (!matchIterator.hasNext()) {
                if (matcher.isOptional()) {
                    continue;
                }
                return false;
            }
            E next = matchIterator.next();
            if (!matcher.match(next)) {
                return false;
            }
        }

        return true;
    }

    @Override
    void setContext(MatchingContext<E> context) {
        super.setContext(context);
        for (ParentMatcher<E> matcher : matchers) {
            matcher.setContext(context);
        }
    }

    @Override
    int register(MatchingContext<E> context) {
        Iterator<ParentMatcher<E>> it = matchers.iterator();

        int index = it.next().register(context);
        this.index = index;

        while (it.hasNext()) {
            it.next().register(context);
        }

        return index;
    }

    @Override
    void initialize(MatchingContext<E> context) {
        for (ParentMatcher<E> matcher : matchers) {
            matcher.initialize(context);
        }
    }

    @Override
    public XMatcher<E> captureAs(String name) {
        return new CapturingGroupMatcher<>(name, this);
    }

    @Override
    public String toString() {
        return "(" + StringUtils.join(matchers, ", ") + ")";
    }

}
