package com.victoryil.wellder.utils

enum class ErrorCodes {
    UNEXPECTED, ARE_ME, USER_NOT_FOUND, FRIEND_YET, SUCCESS;

    fun errorTitle(errorCodes: ErrorCodes) = when (errorCodes) {
        UNEXPECTED -> "unexpected error"
        ARE_ME -> "you can't do this"
        USER_NOT_FOUND -> "user not found"
        FRIEND_YET -> "already your friend"
        SUCCESS -> "success"
    }
    fun errorDescription(errorCodes: ErrorCodes) = when (errorCodes) {
        UNEXPECTED -> "error not known, please try again"
        ARE_ME -> "you cannot do this action with yourself"
        USER_NOT_FOUND -> "the user you are looking for does not exist or is not currently available"
        FRIEND_YET -> "the user you are adding is already your friend"
        SUCCESS -> "success"
    }
}
