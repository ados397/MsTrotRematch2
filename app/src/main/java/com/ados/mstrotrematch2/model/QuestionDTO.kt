package com.ados.mstrotrematch2.model

data class QuestionDTO(var stat: Stat? = Stat.INFO,
                       var title: String? = null,
                       var content: String? = null,
                       var image: String? = null,) {
    enum class Stat {
        INFO, WARNING, ERROR
    }
}

data class GemQuestionDTO(val content: String? = null,
                          val gemCount: Int? = 0
) {
    enum class Stat {
        INFO, WARNING, ERROR
    }
}