object Main extends App {
  println("Started")
  val ciao: Ciao = new Ciao
  ciao.bomba()
}

class Ciao {
  val ciao = 10

  def bomba(): Unit = {
    print("ciao")
    AClass.printSomething("something")
  }
}
