package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer entry is in its life cycle. */
enum class CappedTrailerEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped trailer entry. Its code is unique within a tenant. */
data class CappedTrailerEntry(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var phone: String,
    var discount: Double,
    var status: CappedTrailerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerEntryStatus): Boolean {
        when (status) {
            CappedTrailerEntryStatus.REQUESTED -> return next == CappedTrailerEntryStatus.REVOKED
            CappedTrailerEntryStatus.GRANTED -> {
                when (next) {
                    CappedTrailerEntryStatus.REVOKED -> return true
                }
                return false
            }
            CappedTrailerEntryStatus.REVOKED -> {
                when (next) {
                    CappedTrailerEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() + discount) * 100) / 100

    fun snapshot(): CappedTrailerEntry {
        return CappedTrailerEntry(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            phone = phone,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped trailer entry in status requested. */
fun newCappedTrailerEntry(code: String, name: String): CappedTrailerEntry {
    return CappedTrailerEntry(
        code = code,
        name = name,
        durationMinutes = 0,
        phone = "",
        discount = 0.0,
        status = CappedTrailerEntryStatus.REQUESTED,
    )
}
