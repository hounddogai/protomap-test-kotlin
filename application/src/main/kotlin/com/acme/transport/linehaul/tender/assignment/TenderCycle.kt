package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender cycle is in its life cycle. */
enum class TenderCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tender cycle and its current status. */
data class TenderCycle(
    var code: String,
    var title: String,
    var endsAt: Int,
    var source: String,
    var distanceKm: Double,
    var contactEmail: String,
    var trailerId: String,
    var batchSize: Int,
    var status: TenderCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderCycleStatus): Boolean {
        when (status) {
            TenderCycleStatus.CREATED -> {
                when (next) {
                    TenderCycleStatus.VALIDATED, TenderCycleStatus.RETIRED -> return true
                }
                return false
            }
            TenderCycleStatus.VALIDATED -> {
                when (next) {
                    TenderCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            TenderCycleStatus.PUBLISHED -> {
                when (next) {
                    TenderCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderCycleStatus) {
        if (!canMoveTo(next)) {
            error("tender cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tender cycle"} $code"

    fun weightedTotal(): Double = round((endsAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TenderCycle {
        return TenderCycle(
            code = code,
            title = title,
            endsAt = endsAt,
            source = source,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            trailerId = trailerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tender cycle in status created. */
fun newTenderCycle(code: String, title: String): TenderCycle {
    return TenderCycle(
        code = code,
        title = title,
        endsAt = 0,
        source = "",
        distanceKm = 0.0,
        contactEmail = "",
        trailerId = "",
        batchSize = 0,
        status = TenderCycleStatus.CREATED,
    )
}
