package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary roster record is in its life cycle. */
enum class PrimaryRosterRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary roster record, with what the planners need to act on it. */
data class PrimaryRosterRecord(
    var code: String,
    var name: String,
    var currency: String,
    var depot: String,
    var lane: String,
    var batchSize: Int,
    var timezone: String,
    var loadKg: Double,
    var status: PrimaryRosterRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterRecordStatus): Boolean {
        when (status) {
            PrimaryRosterRecordStatus.REQUESTED -> {
                when (next) {
                    PrimaryRosterRecordStatus.GRANTED, PrimaryRosterRecordStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryRosterRecordStatus.GRANTED -> return next == PrimaryRosterRecordStatus.REVOKED
            PrimaryRosterRecordStatus.REVOKED -> {
                when (next) {
                    PrimaryRosterRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRosterRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary roster record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary roster record"} $code"

    fun weightedTotal(): Double = round((loadKg - batchSize.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRosterRecord {
        return PrimaryRosterRecord(
            code = code,
            name = name,
            currency = currency,
            depot = depot,
            lane = lane,
            batchSize = batchSize,
            timezone = timezone,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary roster record in status requested. */
fun newPrimaryRosterRecord(code: String, name: String): PrimaryRosterRecord {
    return PrimaryRosterRecord(
        code = code,
        name = name,
        currency = "",
        depot = "",
        lane = "",
        batchSize = 0,
        timezone = "",
        loadKg = 0.0,
        status = PrimaryRosterRecordStatus.REQUESTED,
    )
}
