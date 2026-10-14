package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global drop exception is in its life cycle. */
enum class GlobalDropExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global drop exception. Its code is unique within a tenant. */
data class GlobalDropException(
    var code: String,
    var name: String,
    var sequence: Int,
    var createdAt: Int,
    var limit: Int,
    var expiresAt: Int,
    var lane: String,
    var status: GlobalDropExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDropExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDropExceptionStatus): Boolean {
        when (status) {
            GlobalDropExceptionStatus.IDLE -> {
                when (next) {
                    GlobalDropExceptionStatus.READY -> return true
                }
                return false
            }
            GlobalDropExceptionStatus.WARMING -> {
                when (next) {
                    GlobalDropExceptionStatus.STOPPED -> return true
                }
                return false
            }
            GlobalDropExceptionStatus.READY -> {
                when (next) {
                    GlobalDropExceptionStatus.STOPPED -> return true
                }
                return false
            }
            GlobalDropExceptionStatus.DRAINING -> {
                when (next) {
                    GlobalDropExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDropExceptionStatus) {
        if (!canMoveTo(next)) {
            error("global drop exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global drop exception"} $code"

    fun adjustedVolume(): Double = round(expiresAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): GlobalDropException {
        return GlobalDropException(
            code = code,
            name = name,
            sequence = sequence,
            createdAt = createdAt,
            limit = limit,
            expiresAt = expiresAt,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global drop exception in status idle. */
fun newGlobalDropException(code: String, name: String): GlobalDropException {
    return GlobalDropException(
        code = code,
        name = name,
        sequence = 0,
        createdAt = 0,
        limit = 0,
        expiresAt = 0,
        lane = "",
        status = GlobalDropExceptionStatus.IDLE,
    )
}
