package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender rollup records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender rollup is in its life cycle. */
enum class TenderRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tender rollup as the tender team keeps it. */
data class TenderRollup(
    var code: String,
    var label: String,
    var limit: Int,
    var startsAt: Int,
    var weight: Double,
    var locale: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var sequence: Int,
    var status: TenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderRollupStatus): Boolean {
        when (status) {
            TenderRollupStatus.PLANNED -> {
                when (next) {
                    TenderRollupStatus.CANCELLED -> return true
                }
                return false
            }
            TenderRollupStatus.SCHEDULED -> return next == TenderRollupStatus.COMPLETED
            TenderRollupStatus.IN_PROGRESS -> return next == TenderRollupStatus.COMPLETED || next == TenderRollupStatus.CANCELLED
            TenderRollupStatus.COMPLETED -> return next == TenderRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("tender rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((sequence.toDouble() + weight) * 100) / 100

    fun snapshot(): TenderRollup {
        return TenderRollup(
            code = code,
            label = label,
            limit = limit,
            startsAt = startsAt,
            weight = weight,
            locale = locale,
            title = title,
            attributes = attributes,
            dock = dock,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tender rollup in status planned. */
fun newTenderRollup(code: String, label: String): TenderRollup {
    return TenderRollup(
        code = code,
        label = label,
        limit = 0,
        startsAt = 0,
        weight = 0.0,
        locale = "",
        title = "",
        attributes = mutableMapOf<String, String>(),
        dock = "",
        sequence = 0,
        status = TenderRollupStatus.PLANNED,
    )
}
