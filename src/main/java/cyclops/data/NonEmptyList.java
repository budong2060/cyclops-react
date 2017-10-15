package cyclops.data;


import com.aol.cyclops2.matching.Deconstruct.Deconstruct2;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.control.Option;
import cyclops.control.lazy.Trampoline;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"head,tail"})
public class NonEmptyList<T> implements Deconstruct2<T,ImmutableList<T>>, ImmutableList<T>, ImmutableList.Some<T> {

    private final T head;
    private final ImmutableList<T> tail;

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }
    public LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(this);
    }
    public static <T> NonEmptyList<T> of(T head, T... value){
        LazySeq<T> list = LazySeq.of(value);
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> of(T head){
        LazySeq<T> list = LazySeq.empty();
        return cons(head,list);
    }

    public static <T> NonEmptyList<T> eager(T head, T... value){
        Seq<T> list = Seq.of(value);
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> eager(T head){
        Seq<T> list = Seq.empty();
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> of(T head, ImmutableList<T> list){
        return cons(head,list);
    }


    public Option<T> get(int pos){
        if(pos==0)
            return Option.of(head);
        return tail.get(pos);

    }

    @Override
    public T getOrElse(int pos, T alt) {
        return get(pos).orElse(alt);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<T> alt) {
        return get(pos).orElseGet(alt);
    }

    public LazySeq<T> asList(){
        return LazySeq.lazy(head,()->tail);
    }

    @Override
    public <R> ImmutableList<R> unitStream(Stream<R> stream) {
        Iterator<R> it = stream.iterator();
        return unitIterator(it);
    }
    @Override
    public <R> ImmutableList<R> unitIterator(Iterator<R> it) {
        if(it.hasNext()){
            return cons(it.next(), LazySeq.fromIterator(it));
        }
        return LazySeq.empty();
    }

    @Override
    public ImmutableList<T> emptyUnit() {
        return Seq.empty();
    }

    @Override
    public ImmutableList<T> drop(long num) {
        if(num>=size())
            return Seq.empty();

        return unitStream(stream().drop(num));
    }

    @Override
    public ImmutableList<T> take(long num) {
        if(num==0){
            return LazySeq.empty();
        }
        return cons(head,tail.take(num-1));
    }

    public NonEmptyList<T> prepend(T value){
        return cons(value,asList());
    }

    @Override
    public NonEmptyList<T> prependAll(Iterable<T> value) {
        LazySeq<T> list = asList().prependAll(value);
        return cons(list.fold(c->c.head(), nil->null),list.drop(1));
    }

    @Override
    public ImmutableList<T> append(T value) {
        return of(head,tail.append(value));
    }

    @Override
    public NonEmptyList<T> appendAll(Iterable<T> value) {
        return of(head,tail.appendAll(value));
    }

    @Override
    public ImmutableList<T> tail() {
        return tail;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public NonEmptyList<T> reverse() {
        return of(head).prependAll(tail);
    }

    public NonEmptyList<T> prependAll(NonEmptyList<T> value){
        return value.prependAll(this);
    }

    public ImmutableList<T> filter(Predicate<? super T> pred){
        return asList().filter(pred);
    }


    public <R> NonEmptyList<R> map(Function<? super T, ? extends R> fn) {
        return NonEmptyList.of(fn.apply(head),tail.map(fn));
    }

    @Override
    public NonEmptyList<T> peek(Consumer<? super T> c) {
        return (NonEmptyList<T>)ImmutableList.Some.super.peek(c);
    }

    @Override
    public <R> NonEmptyList<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (NonEmptyList<R>)ImmutableList.Some.super.trampoline(mapper);
    }

    @Override
    public <R> NonEmptyList<R> retry(Function<? super T, ? extends R> fn) {
        return (NonEmptyList<R>)ImmutableList.Some.super.retry(fn);
    }

    @Override
    public <R> NonEmptyList<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (NonEmptyList<R>)ImmutableList.Some.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return fn1.apply(this);
    }

    @Override
    public NonEmptyList<T> onEmpty(T value) {
        return this;
    }

    @Override
    public NonEmptyList<T> onEmptyGet(Supplier<? extends T> supplier) {
        return this;
    }

    @Override
    public <X extends Throwable> NonEmptyList<T> onEmptyThrow(Supplier<? extends X> supplier) {
        return this;
    }

    @Override
    public NonEmptyList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
        return this;
    }


    @Override
    public <R> ImmutableList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return asList().flatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return asList().flatMapI(fn);
    }

    public <R> NonEmptyList<R> flatMapNel(Function<? super T, ? extends NonEmptyList<R>> fn) {
        return fn.apply(head).appendAll(tail.flatMap(fn));

    }



    public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
        return asList().foldRight(zero,f);

    }
    public <R> R foldLeft(R zero,BiFunction<R, ? super T, R> f) {
        return asList().foldLeft(zero,f);
    }

    public int size(){
        return 1+tail.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public static <T> NonEmptyList<T> cons(T value, ImmutableList<T> tail){
        return new NonEmptyList<>(value,tail);
    }

    @Override
    public String toString() {
        return stream().join(",","[","]");
    }

    @Override
    public Tuple2<T, ImmutableList<T>> unapply() {
        return Tuple.tuple(head,tail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if(o instanceof ImmutableList){
            ImmutableList<T> im =(ImmutableList<T>)o;
            return equalToIteration(im);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), head, tail);
    }
}
