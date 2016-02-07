public abstract class Task<T, E> {

	public abstract T run(E arg);

	public final TaskFuture<T> executeLater(E arg) {
		return null;
	}

	public final SpawnedTaskFuture<T> spawn(E arg) {
		return null;
	}

}
