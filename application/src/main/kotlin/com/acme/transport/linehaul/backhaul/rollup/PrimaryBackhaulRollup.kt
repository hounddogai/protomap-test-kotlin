package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary backhaul rollup records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a primary backhaul rollup is in its life cycle. */
enum class PrimaryBackhaulRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary backhaul rollup. Its code is unique within a tenant. */
data class PrimaryBackhaulRollup(
    var code: String,
    var title: String,
    var unit: String,
    var segment: String,
    var notes: String,
    var startsAt: Int,
    var loadKg: Double,
    var description: String,
    var status: PrimaryBackhaulRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBackhaulRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBackhaulRollupStatus): Boolean {
        when (status) {
            PrimaryBackhaulRollupStatus.REQUESTED -> return next == PrimaryBackhaulRollupStatus.REVOKED || next == PrimaryBackhaulRollupStatus.EXPIRED
            PrimaryBackhaulRollupStatus.GRANTED -> return next == PrimaryBackhaulRollupStatus.REVOKED || next == PrimaryBackhaulRollupStatus.EXPIRED
            PrimaryBackhaulRollupStatus.REVOKED -> return next == PrimaryBackhaulRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryBackhaulRollupStatus) {
        if (!canMoveTo(next)) {
            error("primary backhaul rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round((startsAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): PrimaryBackhaulRollup {
        return PrimaryBackhaulRollup(
            code = code,
            title = title,
            unit = unit,
            segment = segment,
            notes = notes,
            startsAt = startsAt,
            loadKg = loadKg,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary backhaul rollup in status requested. */
fun newPrimaryBackhaulRollup(code: String, title: String): PrimaryBackhaulRollup {
    return PrimaryBackhaulRollup(
        code = code,
        title = title,
        unit = "",
        segment = "",
        notes = "",
        startsAt = 0,
        loadKg = 0.0,
        description = "",
        status = PrimaryBackhaulRollupStatus.REQUESTED,
    )
}
