class Scientist {
	// region Lab, Auditorium;
	Work research;// in Lab;
	Work teaching;// in Auditorium;

	public void doJob()/* writes Lab, Auditorium */{
		TaskFuture writing = new Task<ResearchPaper, Work>() {
			public ResearchPaper run(Work research) {
				research.justDoIt();
				return new ResearchPaper();
			}
		}.executeLater(research);

		while (!writing.isDone()) {
			new Task<Void, Work/* , writes Auditorium */>() {
				public Void run(Work teaching) {
					teaching.justDoIt();
					return null;
				}
			}.spawn(teaching).join();
		}
		publish(writing.getValue());
	}

	private void publish(Object work) {

	}
}

class Work {
	public void justDoIt() {

	}
}

class ResearchPaper {

}
