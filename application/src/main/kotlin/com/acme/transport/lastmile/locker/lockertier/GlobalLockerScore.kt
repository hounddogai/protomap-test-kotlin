package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global locker score is in its life cycle. */
enum class GlobalLockerScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global locker score and its current status. */
data class GlobalLockerScore(
    var code: String,
    var name: String,
    var active: Boolean,
    var category: String,
    var segment: String,
    var archived: Boolean,
    var pinned: Boolean,
    var description: String,
    var source: String,
    var loadKg: Double,
    var ceiling: Double,
    var status: GlobalLockerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLockerScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLockerScoreStatus): Boolean {
        when (status) {
            GlobalLockerScoreStatus.NEW -> return next == GlobalLockerScoreStatus.DONE || next == GlobalLockerScoreStatus.FAILED
            GlobalLockerScoreStatus.QUEUED -> {
                when (next) {
                    GlobalLockerScoreStatus.FAILED -> return true
                }
                return false
            }
            GlobalLockerScoreStatus.RUNNING -> {
                when (next) {
                    GlobalLockerScoreStatus.DONE, GlobalLockerScoreStatus.FAILED -> return true
                }
                return false
            }
            GlobalLockerScoreStatus.DONE -> return next == GlobalLockerScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalLockerScoreStatus) {
        if (!canMoveTo(next)) {
            error("global locker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((ceiling + loadKg) * 100) / 100

    fun snapshot(): GlobalLockerScore {
        return GlobalLockerScore(
            code = code,
            name = name,
            active = active,
            category = category,
            segment = segment,
            archived = archived,
            pinned = pinned,
            description = description,
            source = source,
            loadKg = loadKg,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global locker score in status new. */
fun newGlobalLockerScore(code: String, name: String): GlobalLockerScore {
    return GlobalLockerScore(
        code = code,
        name = name,
        active = false,
        category = "",
        segment = "",
        archived = false,
        pinned = false,
        description = "",
        source = "",
        loadKg = 0.0,
        ceiling = 0.0,
        status = GlobalLockerScoreStatus.NEW,
    )
}
