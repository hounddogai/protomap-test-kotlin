package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient freight invoice profile is in its life cycle. */
enum class LenientFreightInvoiceProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient freight invoice profile, with what the planners need to act on it. */
data class LenientFreightInvoiceProfile(
    var code: String,
    var name: String,
    var endsAt: Int,
    var attempts: Int,
    var ownerId: String,
    var distanceKm: Double,
    var notes: String,
    var category: String,
    var visible: Boolean,
    var shiftCode: String,
    var status: LenientFreightInvoiceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceProfileStatus): Boolean {
        when (status) {
            LenientFreightInvoiceProfileStatus.IDLE -> {
                when (next) {
                    LenientFreightInvoiceProfileStatus.WARMING, LenientFreightInvoiceProfileStatus.STOPPED -> return true
                }
                return false
            }
            LenientFreightInvoiceProfileStatus.WARMING -> return next == LenientFreightInvoiceProfileStatus.STOPPED
            LenientFreightInvoiceProfileStatus.READY -> return next == LenientFreightInvoiceProfileStatus.DRAINING
            LenientFreightInvoiceProfileStatus.DRAINING -> return next == LenientFreightInvoiceProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceProfileStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((endsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): LenientFreightInvoiceProfile {
        return LenientFreightInvoiceProfile(
            code = code,
            name = name,
            endsAt = endsAt,
            attempts = attempts,
            ownerId = ownerId,
            distanceKm = distanceKm,
            notes = notes,
            category = category,
            visible = visible,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient freight invoice profile in status idle. */
fun newLenientFreightInvoiceProfile(code: String, name: String): LenientFreightInvoiceProfile {
    return LenientFreightInvoiceProfile(
        code = code,
        name = name,
        endsAt = 0,
        attempts = 0,
        ownerId = "",
        distanceKm = 0.0,
        notes = "",
        category = "",
        visible = false,
        shiftCode = "",
        status = LenientFreightInvoiceProfileStatus.IDLE,
    )
}
