package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional rate card snapshot records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a regional rate card snapshot is in its life cycle. */
enum class RegionalRateCardSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional rate card snapshot. Its code is unique within a tenant. */
data class RegionalRateCardSnapshot(
    var code: String,
    var name: String,
    var rate: Double,
    var capacity: Int,
    var summary: String,
    var ceiling: Double,
    var revision: Int,
    var etaMinutes: Int,
    var title: String,
    var description: String,
    var status: RegionalRateCardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRateCardSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRateCardSnapshotStatus): Boolean {
        when (status) {
            RegionalRateCardSnapshotStatus.DRAFT -> {
                when (next) {
                    RegionalRateCardSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRateCardSnapshotStatus.PENDING -> {
                when (next) {
                    RegionalRateCardSnapshotStatus.ACTIVE, RegionalRateCardSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRateCardSnapshotStatus.ACTIVE -> return next == RegionalRateCardSnapshotStatus.SUSPENDED || next == RegionalRateCardSnapshotStatus.CLOSED
            RegionalRateCardSnapshotStatus.SUSPENDED -> {
                when (next) {
                    RegionalRateCardSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRateCardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional rate card snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round((revision.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RegionalRateCardSnapshot {
        return RegionalRateCardSnapshot(
            code = code,
            name = name,
            rate = rate,
            capacity = capacity,
            summary = summary,
            ceiling = ceiling,
            revision = revision,
            etaMinutes = etaMinutes,
            title = title,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional rate card snapshot in status draft. */
fun newRegionalRateCardSnapshot(code: String, name: String): RegionalRateCardSnapshot {
    return RegionalRateCardSnapshot(
        code = code,
        name = name,
        rate = 0.0,
        capacity = 0,
        summary = "",
        ceiling = 0.0,
        revision = 0,
        etaMinutes = 0,
        title = "",
        description = "",
        status = RegionalRateCardSnapshotStatus.DRAFT,
    )
}
