package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual ev charge score is in its life cycle. */
enum class ManualEvChargeScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One manual ev charge score, with what the planners need to act on it. */
data class ManualEvChargeScore(
    var code: String,
    var name: String,
    var title: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var trailerId: String,
    var visible: Boolean,
    var cost: Double,
    var maximum: Double,
    var retries: Int,
    var ratio: Double,
    var status: ManualEvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualEvChargeScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualEvChargeScoreStatus): Boolean {
        when (status) {
            ManualEvChargeScoreStatus.NEW -> return next == ManualEvChargeScoreStatus.QUEUED || next == ManualEvChargeScoreStatus.DONE
            ManualEvChargeScoreStatus.QUEUED -> return next == ManualEvChargeScoreStatus.RUNNING || next == ManualEvChargeScoreStatus.FAILED
            ManualEvChargeScoreStatus.RUNNING -> return next == ManualEvChargeScoreStatus.FAILED
            ManualEvChargeScoreStatus.DONE -> return next == ManualEvChargeScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualEvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("manual ev charge score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((ratio - retries.toDouble()) * 100) / 100

    fun snapshot(): ManualEvChargeScore {
        return ManualEvChargeScore(
            code = code,
            name = name,
            title = title,
            endsAt = endsAt,
            tags = tags,
            trailerId = trailerId,
            visible = visible,
            cost = cost,
            maximum = maximum,
            retries = retries,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual ev charge score in status new. */
fun newManualEvChargeScore(code: String, name: String): ManualEvChargeScore {
    return ManualEvChargeScore(
        code = code,
        name = name,
        title = "",
        endsAt = 0,
        tags = mutableListOf<String>(),
        trailerId = "",
        visible = false,
        cost = 0.0,
        maximum = 0.0,
        retries = 0,
        ratio = 0.0,
        status = ManualEvChargeScoreStatus.NEW,
    )
}
