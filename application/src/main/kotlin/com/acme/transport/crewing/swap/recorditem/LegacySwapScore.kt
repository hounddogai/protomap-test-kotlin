package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy swap score is in its life cycle. */
enum class LegacySwapScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy swap score and its current status. */
data class LegacySwapScore(
    var code: String,
    var label: String,
    var description: String,
    var capacity: Int,
    var retries: Int,
    var tags: MutableList<String>,
    var status: LegacySwapScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySwapScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySwapScoreStatus): Boolean {
        when (status) {
            LegacySwapScoreStatus.NEW -> return next == LegacySwapScoreStatus.DONE
            LegacySwapScoreStatus.QUEUED -> {
                when (next) {
                    LegacySwapScoreStatus.RUNNING, LegacySwapScoreStatus.DONE -> return true
                }
                return false
            }
            LegacySwapScoreStatus.RUNNING -> {
                when (next) {
                    LegacySwapScoreStatus.FAILED -> return true
                }
                return false
            }
            LegacySwapScoreStatus.DONE -> return next == LegacySwapScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacySwapScoreStatus) {
        if (!canMoveTo(next)) {
            error("legacy swap score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round(retries.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): LegacySwapScore {
        return LegacySwapScore(
            code = code,
            label = label,
            description = description,
            capacity = capacity,
            retries = retries,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy swap score in status new. */
fun newLegacySwapScore(code: String, label: String): LegacySwapScore {
    return LegacySwapScore(
        code = code,
        label = label,
        description = "",
        capacity = 0,
        retries = 0,
        tags = mutableListOf<String>(),
        status = LegacySwapScoreStatus.NEW,
    )
}
