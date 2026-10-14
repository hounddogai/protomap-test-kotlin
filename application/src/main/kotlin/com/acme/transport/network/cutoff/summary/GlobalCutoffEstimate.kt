package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cutoff estimate is in its life cycle. */
enum class GlobalCutoffEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global cutoff estimate and its current status. */
data class GlobalCutoffEstimate(
    var code: String,
    var label: String,
    var checksum: String,
    var approved: Boolean,
    var revision: Int,
    var pallets: Int,
    var weight: Double,
    var startsAt: Int,
    var stopCount: Int,
    var status: GlobalCutoffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffEstimateStatus): Boolean {
        when (status) {
            GlobalCutoffEstimateStatus.REQUESTED -> {
                when (next) {
                    GlobalCutoffEstimateStatus.REVOKED, GlobalCutoffEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalCutoffEstimateStatus.GRANTED -> return next == GlobalCutoffEstimateStatus.EXPIRED
            GlobalCutoffEstimateStatus.REVOKED -> {
                when (next) {
                    GlobalCutoffEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"global cutoff estimate"} $code"

    fun netValue(): Double = round(revision.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalCutoffEstimate {
        return GlobalCutoffEstimate(
            code = code,
            label = label,
            checksum = checksum,
            approved = approved,
            revision = revision,
            pallets = pallets,
            weight = weight,
            startsAt = startsAt,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global cutoff estimate in status requested. */
fun newGlobalCutoffEstimate(code: String, label: String): GlobalCutoffEstimate {
    return GlobalCutoffEstimate(
        code = code,
        label = label,
        checksum = "",
        approved = false,
        revision = 0,
        pallets = 0,
        weight = 0.0,
        startsAt = 0,
        stopCount = 0,
        status = GlobalCutoffEstimateStatus.REQUESTED,
    )
}
