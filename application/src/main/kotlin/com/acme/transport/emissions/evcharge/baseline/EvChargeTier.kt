package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge tier is in its life cycle. */
enum class EvChargeTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A ev charge tier. Its code is unique within a tenant. */
data class EvChargeTier(
    var code: String,
    var name: String,
    var country: String,
    var graceDays: Int,
    var endsAt: Int,
    var status: EvChargeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeTierStatus): Boolean {
        when (status) {
            EvChargeTierStatus.OPEN -> {
                when (next) {
                    EvChargeTierStatus.REOPENED -> return true
                }
                return false
            }
            EvChargeTierStatus.IN_REVIEW -> {
                when (next) {
                    EvChargeTierStatus.RESOLVED, EvChargeTierStatus.REOPENED -> return true
                }
                return false
            }
            EvChargeTierStatus.RESOLVED -> {
                when (next) {
                    EvChargeTierStatus.REOPENED, EvChargeTierStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeTierStatus.REOPENED -> return next == EvChargeTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EvChargeTierStatus) {
        if (!canMoveTo(next)) {
            error("ev charge tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((endsAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): EvChargeTier {
        return EvChargeTier(
            code = code,
            name = name,
            country = country,
            graceDays = graceDays,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge tier in status open. */
fun newEvChargeTier(code: String, name: String): EvChargeTier {
    return EvChargeTier(
        code = code,
        name = name,
        country = "",
        graceDays = 0,
        endsAt = 0,
        status = EvChargeTierStatus.OPEN,
    )
}
