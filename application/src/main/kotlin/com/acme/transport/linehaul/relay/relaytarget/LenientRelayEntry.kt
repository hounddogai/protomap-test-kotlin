package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient relay entry is in its life cycle. */
enum class LenientRelayEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient relay entry as the relay team keeps it. */
data class LenientRelayEntry(
    var code: String,
    var label: String,
    var summary: String,
    var name: String,
    var revision: Int,
    var pallets: Int,
    var amount: Double,
    var checksum: String,
    var threshold: Double,
    var status: LenientRelayEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayEntryStatus): Boolean {
        when (status) {
            LenientRelayEntryStatus.REQUESTED -> {
                when (next) {
                    LenientRelayEntryStatus.GRANTED, LenientRelayEntryStatus.REVOKED -> return true
                }
                return false
            }
            LenientRelayEntryStatus.GRANTED -> return next == LenientRelayEntryStatus.REVOKED || next == LenientRelayEntryStatus.EXPIRED
            LenientRelayEntryStatus.REVOKED -> return next == LenientRelayEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientRelayEntryStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lenient relay entry"} $code"

    fun netVolume(): Double = round((pallets.toDouble() + amount) * 100) / 100

    fun snapshot(): LenientRelayEntry {
        return LenientRelayEntry(
            code = code,
            label = label,
            summary = summary,
            name = name,
            revision = revision,
            pallets = pallets,
            amount = amount,
            checksum = checksum,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient relay entry in status requested. */
fun newLenientRelayEntry(code: String, label: String): LenientRelayEntry {
    return LenientRelayEntry(
        code = code,
        label = label,
        summary = "",
        name = "",
        revision = 0,
        pallets = 0,
        amount = 0.0,
        checksum = "",
        threshold = 0.0,
        status = LenientRelayEntryStatus.REQUESTED,
    )
}
