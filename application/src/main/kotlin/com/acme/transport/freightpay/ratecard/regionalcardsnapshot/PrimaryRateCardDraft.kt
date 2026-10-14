package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary rate card draft is in its life cycle. */
enum class PrimaryRateCardDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary rate card draft. Its code is unique within a tenant. */
data class PrimaryRateCardDraft(
    var code: String,
    var name: String,
    var notes: String,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var ceiling: Double,
    var endsAt: Int,
    var description: String,
    var status: PrimaryRateCardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRateCardDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRateCardDraftStatus): Boolean {
        when (status) {
            PrimaryRateCardDraftStatus.PLANNED -> {
                when (next) {
                    PrimaryRateCardDraftStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryRateCardDraftStatus.SCHEDULED -> {
                when (next) {
                    PrimaryRateCardDraftStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryRateCardDraftStatus.IN_PROGRESS -> return next == PrimaryRateCardDraftStatus.COMPLETED || next == PrimaryRateCardDraftStatus.CANCELLED
            PrimaryRateCardDraftStatus.COMPLETED -> return next == PrimaryRateCardDraftStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryRateCardDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary rate card draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun effectiveLoad(): Double = round((ceiling + fuelLiters) * 100) / 100

    fun snapshot(): PrimaryRateCardDraft {
        return PrimaryRateCardDraft(
            code = code,
            name = name,
            notes = notes,
            fuelLiters = fuelLiters,
            tags = tags,
            ceiling = ceiling,
            endsAt = endsAt,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary rate card draft in status planned. */
fun newPrimaryRateCardDraft(code: String, name: String): PrimaryRateCardDraft {
    return PrimaryRateCardDraft(
        code = code,
        name = name,
        notes = "",
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        ceiling = 0.0,
        endsAt = 0,
        description = "",
        status = PrimaryRateCardDraftStatus.PLANNED,
    )
}
