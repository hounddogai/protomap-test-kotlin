package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff draft records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff draft is in its life cycle. */
enum class TariffDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff draft as the tariff team keeps it. */
data class TariffDraft(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var capacity: Int,
    var minimum: Double,
    var ceiling: Double,
    var stopCount: Int,
    var attempts: Int,
    var status: TariffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffDraftStatus): Boolean {
        when (status) {
            TariffDraftStatus.PLANNED -> return next == TariffDraftStatus.CANCELLED
            TariffDraftStatus.SCHEDULED -> {
                when (next) {
                    TariffDraftStatus.COMPLETED, TariffDraftStatus.CANCELLED -> return true
                }
                return false
            }
            TariffDraftStatus.IN_PROGRESS -> return next == TariffDraftStatus.CANCELLED
            TariffDraftStatus.COMPLETED -> {
                when (next) {
                    TariffDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffDraftStatus) {
        if (!canMoveTo(next)) {
            error("tariff draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(attempts.toDouble() * ceiling * 100) / 100

    fun snapshot(): TariffDraft {
        return TariffDraft(
            code = code,
            name = name,
            tags = tags,
            capacity = capacity,
            minimum = minimum,
            ceiling = ceiling,
            stopCount = stopCount,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff draft in status planned. */
fun newTariffDraft(code: String, name: String): TariffDraft {
    return TariffDraft(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        capacity = 0,
        minimum = 0.0,
        ceiling = 0.0,
        stopCount = 0,
        attempts = 0,
        status = TariffDraftStatus.PLANNED,
    )
}
