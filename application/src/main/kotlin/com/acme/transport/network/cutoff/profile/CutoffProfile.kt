package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff profile records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff profile is in its life cycle. */
enum class CutoffProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cutoff profile as the cutoff team keeps it. */
data class CutoffProfile(
    var code: String,
    var name: String,
    var source: String,
    var windowDays: Int,
    var endsAt: Int,
    var version: Int,
    var unit: String,
    var status: CutoffProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffProfileStatus): Boolean {
        when (status) {
            CutoffProfileStatus.OPEN -> {
                when (next) {
                    CutoffProfileStatus.RESOLVED -> return true
                }
                return false
            }
            CutoffProfileStatus.IN_REVIEW -> {
                when (next) {
                    CutoffProfileStatus.REOPENED, CutoffProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            CutoffProfileStatus.RESOLVED -> {
                when (next) {
                    CutoffProfileStatus.REOPENED -> return true
                }
                return false
            }
            CutoffProfileStatus.REOPENED -> return next == CutoffProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CutoffProfileStatus) {
        if (!canMoveTo(next)) {
            error("cutoff profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((windowDays.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CutoffProfile {
        return CutoffProfile(
            code = code,
            name = name,
            source = source,
            windowDays = windowDays,
            endsAt = endsAt,
            version = version,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff profile in status open. */
fun newCutoffProfile(code: String, name: String): CutoffProfile {
    return CutoffProfile(
        code = code,
        name = name,
        source = "",
        windowDays = 0,
        endsAt = 0,
        version = 0,
        unit = "",
        status = CutoffProfileStatus.OPEN,
    )
}
