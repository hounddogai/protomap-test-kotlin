package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling tender rollup records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a rolling tender rollup is in its life cycle. */
enum class RollingTenderRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling tender rollup as the tender team keeps it. */
data class RollingTenderRollup(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var revision: Int,
    var dock: String,
    var phone: String,
    var status: RollingTenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTenderRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTenderRollupStatus): Boolean {
        when (status) {
            RollingTenderRollupStatus.CREATED -> {
                when (next) {
                    RollingTenderRollupStatus.VALIDATED, RollingTenderRollupStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingTenderRollupStatus.VALIDATED -> return next == RollingTenderRollupStatus.RETIRED
            RollingTenderRollupStatus.PUBLISHED -> {
                when (next) {
                    RollingTenderRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("rolling tender rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((expiresAt.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): RollingTenderRollup {
        return RollingTenderRollup(
            code = code,
            title = title,
            expiresAt = expiresAt,
            revision = revision,
            dock = dock,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling tender rollup in status created. */
fun newRollingTenderRollup(code: String, title: String): RollingTenderRollup {
    return RollingTenderRollup(
        code = code,
        title = title,
        expiresAt = 0,
        revision = 0,
        dock = "",
        phone = "",
        status = RollingTenderRollupStatus.CREATED,
    )
}
