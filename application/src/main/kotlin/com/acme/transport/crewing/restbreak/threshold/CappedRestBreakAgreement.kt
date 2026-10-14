package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rest break agreement is in its life cycle. */
enum class CappedRestBreakAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped rest break agreement. Its code is unique within a tenant. */
data class CappedRestBreakAgreement(
    var code: String,
    var label: String,
    var maximum: Double,
    var margin: Double,
    var loadKg: Double,
    var threshold: Double,
    var status: CappedRestBreakAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRestBreakAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRestBreakAgreementStatus): Boolean {
        when (status) {
            CappedRestBreakAgreementStatus.NEW -> return next == CappedRestBreakAgreementStatus.QUEUED || next == CappedRestBreakAgreementStatus.FAILED
            CappedRestBreakAgreementStatus.QUEUED -> {
                when (next) {
                    CappedRestBreakAgreementStatus.RUNNING -> return true
                }
                return false
            }
            CappedRestBreakAgreementStatus.RUNNING -> {
                when (next) {
                    CappedRestBreakAgreementStatus.FAILED -> return true
                }
                return false
            }
            CappedRestBreakAgreementStatus.DONE -> {
                when (next) {
                    CappedRestBreakAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRestBreakAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped rest break agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((maximum + threshold) * 100) / 100

    fun snapshot(): CappedRestBreakAgreement {
        return CappedRestBreakAgreement(
            code = code,
            label = label,
            maximum = maximum,
            margin = margin,
            loadKg = loadKg,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped rest break agreement in status new. */
fun newCappedRestBreakAgreement(code: String, label: String): CappedRestBreakAgreement {
    return CappedRestBreakAgreement(
        code = code,
        label = label,
        maximum = 0.0,
        margin = 0.0,
        loadKg = 0.0,
        threshold = 0.0,
        status = CappedRestBreakAgreementStatus.NEW,
    )
}
