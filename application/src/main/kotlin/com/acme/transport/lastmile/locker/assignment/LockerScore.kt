package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker score is in its life cycle. */
enum class LockerScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A locker score. Its code is unique within a tenant. */
data class LockerScore(
    var code: String,
    var title: String,
    var version: Int,
    var carrier: String,
    var etaMinutes: Int,
    var notes: String,
    var currency: String,
    var batchSize: Int,
    var startsAt: Int,
    var status: LockerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerScoreStatus): Boolean {
        when (status) {
            LockerScoreStatus.NEW -> return next == LockerScoreStatus.RUNNING || next == LockerScoreStatus.DONE
            LockerScoreStatus.QUEUED -> {
                when (next) {
                    LockerScoreStatus.DONE -> return true
                }
                return false
            }
            LockerScoreStatus.RUNNING -> return next == LockerScoreStatus.DONE
            LockerScoreStatus.DONE -> return next == LockerScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LockerScoreStatus) {
        if (!canMoveTo(next)) {
            error("locker score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"locker score"} $code"

    fun netTotal(): Double = round((version.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LockerScore {
        return LockerScore(
            code = code,
            title = title,
            version = version,
            carrier = carrier,
            etaMinutes = etaMinutes,
            notes = notes,
            currency = currency,
            batchSize = batchSize,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker score in status new. */
fun newLockerScore(code: String, title: String): LockerScore {
    return LockerScore(
        code = code,
        title = title,
        version = 0,
        carrier = "",
        etaMinutes = 0,
        notes = "",
        currency = "",
        batchSize = 0,
        startsAt = 0,
        status = LockerScoreStatus.NEW,
    )
}
