package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card manifest is in its life cycle. */
enum class LenientRateCardManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient rate card manifest and its current status. */
data class LenientRateCardManifest(
    var code: String,
    var label: String,
    var margin: Double,
    var description: String,
    var attempts: Int,
    var channel: String,
    var category: String,
    var carrier: String,
    var status: LenientRateCardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardManifestStatus): Boolean {
        when (status) {
            LenientRateCardManifestStatus.NEW -> {
                when (next) {
                    LenientRateCardManifestStatus.DONE -> return true
                }
                return false
            }
            LenientRateCardManifestStatus.QUEUED -> {
                when (next) {
                    LenientRateCardManifestStatus.RUNNING -> return true
                }
                return false
            }
            LenientRateCardManifestStatus.RUNNING -> return next == LenientRateCardManifestStatus.DONE
            LenientRateCardManifestStatus.DONE -> return next == LenientRateCardManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientRateCardManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(attempts.toDouble() * margin * 100) / 100

    fun snapshot(): LenientRateCardManifest {
        return LenientRateCardManifest(
            code = code,
            label = label,
            margin = margin,
            description = description,
            attempts = attempts,
            channel = channel,
            category = category,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient rate card manifest in status new. */
fun newLenientRateCardManifest(code: String, label: String): LenientRateCardManifest {
    return LenientRateCardManifest(
        code = code,
        label = label,
        margin = 0.0,
        description = "",
        attempts = 0,
        channel = "",
        category = "",
        carrier = "",
        status = LenientRateCardManifestStatus.NEW,
    )
}
