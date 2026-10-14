package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap agreement is in its life cycle. */
enum class SwapAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A swap agreement. Its code is unique within a tenant. */
data class SwapAgreement(
    var code: String,
    var label: String,
    var segment: String,
    var routeCode: String,
    var sequence: Int,
    var margin: Double,
    var status: SwapAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapAgreementStatus): Boolean {
        when (status) {
            SwapAgreementStatus.CREATED -> {
                when (next) {
                    SwapAgreementStatus.VALIDATED -> return true
                }
                return false
            }
            SwapAgreementStatus.VALIDATED -> {
                when (next) {
                    SwapAgreementStatus.RETIRED -> return true
                }
                return false
            }
            SwapAgreementStatus.PUBLISHED -> return next == SwapAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SwapAgreementStatus) {
        if (!canMoveTo(next)) {
            error("swap agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((margin - sequence.toDouble()) * 100) / 100

    fun snapshot(): SwapAgreement {
        return SwapAgreement(
            code = code,
            label = label,
            segment = segment,
            routeCode = routeCode,
            sequence = sequence,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a swap agreement in status created. */
fun newSwapAgreement(code: String, label: String): SwapAgreement {
    return SwapAgreement(
        code = code,
        label = label,
        segment = "",
        routeCode = "",
        sequence = 0,
        margin = 0.0,
        status = SwapAgreementStatus.CREATED,
    )
}
