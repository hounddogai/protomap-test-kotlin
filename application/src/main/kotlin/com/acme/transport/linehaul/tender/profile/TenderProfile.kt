package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender profile records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender profile is in its life cycle. */
enum class TenderProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender profile as the tender team keeps it. */
data class TenderProfile(
    var code: String,
    var name: String,
    var retries: Int,
    var endsAt: Int,
    var quantity: Int,
    var shiftCode: String,
    var summary: String,
    var margin: Double,
    var pinned: Boolean,
    var status: TenderProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderProfileStatus): Boolean {
        when (status) {
            TenderProfileStatus.CREATED -> {
                when (next) {
                    TenderProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            TenderProfileStatus.VALIDATED -> return next == TenderProfileStatus.PUBLISHED || next == TenderProfileStatus.RETIRED
            TenderProfileStatus.PUBLISHED -> return next == TenderProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TenderProfileStatus) {
        if (!canMoveTo(next)) {
            error("tender profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((retries.toDouble() + margin) * 100) / 100

    fun snapshot(): TenderProfile {
        return TenderProfile(
            code = code,
            name = name,
            retries = retries,
            endsAt = endsAt,
            quantity = quantity,
            shiftCode = shiftCode,
            summary = summary,
            margin = margin,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tender profile in status created. */
fun newTenderProfile(code: String, name: String): TenderProfile {
    return TenderProfile(
        code = code,
        name = name,
        retries = 0,
        endsAt = 0,
        quantity = 0,
        shiftCode = "",
        summary = "",
        margin = 0.0,
        pinned = false,
        status = TenderProfileStatus.CREATED,
    )
}
