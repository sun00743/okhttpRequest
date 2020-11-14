package com.mika.requester.listener

/**
 * Created by mika on 2020/11/14.
 */
class HttpParserFactory {

    companion object {

        fun stringParser(): StringParser {
            return StringParser()
        }

    }

}