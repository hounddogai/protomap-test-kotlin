package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge group is in its life cycle. */
enum class TieredEvChargeGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered ev charge group as the ev charge team keeps it. */
data class TieredEvChargeGroup(
    var code: String,
    var title: String,
    var rate: Double,
    var approved: Boolean,
    var sequence: Int,
    var country: String,
    var attributes: MutableMap<String, String>,
    var status: TieredEvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeGroupStatus): Boolean {
        when (status) {
            TieredEvChargeGroupStatus.NEW -> return next == TieredEvChargeGroupStatus.RUNNING || next == TieredEvChargeGroupStatus.FAILED
            TieredEvChargeGroupStatus.QUEUED -> return next == TieredEvChargeGroupStatus.DONE
            TieredEvChargeGroupStatus.RUNNING -> {
                when (next) {
                    TieredEvChargeGroupStatus.DONE -> return true
                }
                return false
            }
            TieredEvChargeGroupStatus.DONE -> return next == TieredEvChargeGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredEvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered ev charge group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(sequence.toDouble() * rate * 100) / 100

    fun snapshot(): TieredEvChargeGroup {
        return TieredEvChargeGroup(
            code = code,
            title = title,
            rate = rate,
            approved = approved,
            sequence = sequence,
            country = country,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered ev charge group in status new. */
fun newTieredEvChargeGroup(code: String, title: String): TieredEvChargeGroup {
    return TieredEvChargeGroup(
        code = code,
        title = title,
        rate = 0.0,
        approved = false,
        sequence = 0,
        country = "",
        attributes = mutableMapOf<String, String>(),
        status = TieredEvChargeGroupStatus.NEW,
    )
}
