package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender estimate is in its life cycle. */
enum class ManualTenderEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual tender estimate as the tender team keeps it. */
data class ManualTenderEstimate(
    var code: String,
    var name: String,
    var score: Double,
    var locale: String,
    var threshold: Double,
    var status: ManualTenderEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderEstimateStatus): Boolean {
        when (status) {
            ManualTenderEstimateStatus.IDLE -> return next == ManualTenderEstimateStatus.DRAINING
            ManualTenderEstimateStatus.WARMING -> {
                when (next) {
                    ManualTenderEstimateStatus.STOPPED -> return true
                }
                return false
            }
            ManualTenderEstimateStatus.READY -> return next == ManualTenderEstimateStatus.DRAINING
            ManualTenderEstimateStatus.DRAINING -> return next == ManualTenderEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualTenderEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual tender estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((threshold + score) * 100) / 100

    fun snapshot(): ManualTenderEstimate {
        return ManualTenderEstimate(
            code = code,
            name = name,
            score = score,
            locale = locale,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual tender estimate in status idle. */
fun newManualTenderEstimate(code: String, name: String): ManualTenderEstimate {
    return ManualTenderEstimate(
        code = code,
        name = name,
        score = 0.0,
        locale = "",
        threshold = 0.0,
        status = ManualTenderEstimateStatus.IDLE,
    )
}
