package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge score is in its life cycle. */
enum class EvChargeScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A ev charge score. Its code is unique within a tenant. */
data class EvChargeScore(
    var code: String,
    var name: String,
    var discount: Double,
    var checksum: String,
    var ratio: Double,
    var status: EvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeScoreStatus): Boolean {
        when (status) {
            EvChargeScoreStatus.CREATED -> return next == EvChargeScoreStatus.VALIDATED || next == EvChargeScoreStatus.RETIRED
            EvChargeScoreStatus.VALIDATED -> {
                when (next) {
                    EvChargeScoreStatus.RETIRED -> return true
                }
                return false
            }
            EvChargeScoreStatus.PUBLISHED -> {
                when (next) {
                    EvChargeScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("ev charge score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge score"} $code"

    fun effectiveLoad(): Double = round((ratio + discount) * 100) / 100

    fun snapshot(): EvChargeScore {
        return EvChargeScore(
            code = code,
            name = name,
            discount = discount,
            checksum = checksum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge score in status created. */
fun newEvChargeScore(code: String, name: String): EvChargeScore {
    return EvChargeScore(
        code = code,
        name = name,
        discount = 0.0,
        checksum = "",
        ratio = 0.0,
        status = EvChargeScoreStatus.CREATED,
    )
}
