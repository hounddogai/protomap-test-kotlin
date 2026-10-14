package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender estimate records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender estimate is in its life cycle. */
enum class TenderEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tender estimate, with what the planners need to act on it. */
data class TenderEstimate(
    var code: String,
    var label: String,
    var reviewer: String,
    var segment: String,
    var pallets: Int,
    var summary: String,
    var budget: Double,
    var ceiling: Double,
    var status: TenderEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderEstimateStatus): Boolean {
        when (status) {
            TenderEstimateStatus.DRAFT -> {
                when (next) {
                    TenderEstimateStatus.ACTIVE, TenderEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            TenderEstimateStatus.PENDING -> {
                when (next) {
                    TenderEstimateStatus.ACTIVE, TenderEstimateStatus.CLOSED -> return true
                }
                return false
            }
            TenderEstimateStatus.ACTIVE -> {
                when (next) {
                    TenderEstimateStatus.CLOSED -> return true
                }
                return false
            }
            TenderEstimateStatus.SUSPENDED -> {
                when (next) {
                    TenderEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderEstimateStatus) {
        if (!canMoveTo(next)) {
            error("tender estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round((pallets.toDouble() - ceiling) * 100) / 100

    fun snapshot(): TenderEstimate {
        return TenderEstimate(
            code = code,
            label = label,
            reviewer = reviewer,
            segment = segment,
            pallets = pallets,
            summary = summary,
            budget = budget,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tender estimate in status draft. */
fun newTenderEstimate(code: String, label: String): TenderEstimate {
    return TenderEstimate(
        code = code,
        label = label,
        reviewer = "",
        segment = "",
        pallets = 0,
        summary = "",
        budget = 0.0,
        ceiling = 0.0,
        status = TenderEstimateStatus.DRAFT,
    )
}
