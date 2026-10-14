package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped backhaul profile is in its life cycle. */
enum class CappedBackhaulProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped backhaul profile, with what the planners need to act on it. */
data class CappedBackhaulProfile(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var unit: String,
    var graceDays: Int,
    var limit: Int,
    var checksum: String,
    var country: String,
    var status: CappedBackhaulProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulProfileStatus): Boolean {
        when (status) {
            CappedBackhaulProfileStatus.DRAFT -> {
                when (next) {
                    CappedBackhaulProfileStatus.ACTIVE, CappedBackhaulProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedBackhaulProfileStatus.PENDING -> return next == CappedBackhaulProfileStatus.SUSPENDED || next == CappedBackhaulProfileStatus.CLOSED
            CappedBackhaulProfileStatus.ACTIVE -> return next == CappedBackhaulProfileStatus.SUSPENDED
            CappedBackhaulProfileStatus.SUSPENDED -> {
                when (next) {
                    CappedBackhaulProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedBackhaulProfileStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round((limit.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedBackhaulProfile {
        return CappedBackhaulProfile(
            code = code,
            name = name,
            pinned = pinned,
            unit = unit,
            graceDays = graceDays,
            limit = limit,
            checksum = checksum,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped backhaul profile in status draft. */
fun newCappedBackhaulProfile(code: String, name: String): CappedBackhaulProfile {
    return CappedBackhaulProfile(
        code = code,
        name = name,
        pinned = false,
        unit = "",
        graceDays = 0,
        limit = 0,
        checksum = "",
        country = "",
        status = CappedBackhaulProfileStatus.DRAFT,
    )
}
