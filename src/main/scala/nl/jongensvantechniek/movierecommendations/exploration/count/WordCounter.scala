package nl.jongensvantechniek.movierecommendations.exploration.count

import nl.jongensvantechniek.movierecommendations.application.SparkManager

/**
  *
  */
object WordCounter {

  private val sparkManager = SparkManager
  /**
    *
    * @param dataSourcePath
    * @param splitWordsOnRegex
    * @return
    */
  def getCountOfEachWord(dataSourcePath: String, splitWordsOnRegex: String = "\\W+"): Seq[(Int, String)] =  {
    sparkManager.init("Count of words occurrences")
    // Load up each line of the ratings data into an RDD
    val lines = sparkManager.sc.textFile(dataSourcePath)

    // Split into words separated by a space character
    val words = lines.flatMap(x => x.split(splitWordsOnRegex))

    // Normalize everything to lowercase
    val normalizedWords = words.map(x => x.toLowerCase)

    // Count up the occurrences of each word

    //We have two options: return a map or an RDD, we choose  to return an RDD, which is a scalable path

    // option 1: Return a map of words and the count of each occurrence
    //normalizedWords.countByValue()

    // option 2: Return an RDD instead of a map, to be able to fully utilize scalability by Spark
    val wordsCount = normalizedWords.map(x => (x, 1)).reduceByKey((x,y) => x + y)

    // Return an RDD sorted by occurrence count, instead of the words alphanumerically: (2, foobar) instead of (foobar, 2)
    // Collect the results from the RDD (This kicks off computing the DAG and actually executes the job)
    val result = wordsCount.map(x => (x._2, x._1)).sortByKey().collect()
    sparkManager.close()

    result
  }

}
