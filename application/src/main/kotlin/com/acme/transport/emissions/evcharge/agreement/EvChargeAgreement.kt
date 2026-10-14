package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge agreement records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a ev charge agreement is in its life cycle. */
enum class EvChargeAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A ev charge agreement. Its code is unique within a tenant. */
data class EvChargeAgreement(
    var code: String,
    var title: String,
    var batchSize: Int,
    var score: Double,
    var lane: String,
    var trailerId: String,
    var visible: Boolean,
    var phone: String,
    var startsAt: Int,
    var checksum: String,
    var status: EvChargeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeAgreementStatus): Boolean {
        when (status) {
            EvChargeAgreementStatus.CREATED -> return next == EvChargeAgreementStatus.RETIRED
            EvChargeAgreementStatus.VALIDATED -> return next == EvChargeAgreementStatus.PUBLISHED
            EvChargeAgreementStatus.PUBLISHED -> {
                when (next) {
                    EvChargeAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("ev charge agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((score + batchSize.toDouble()) * 100) / 100

    fun snapshot(): EvChargeAgreement {
        return EvChargeAgreement(
            code = code,
            title = title,
            batchSize = batchSize,
            score = score,
            lane = lane,
            trailerId = trailerId,
            visible = visible,
            phone = phone,
            startsAt = startsAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a ev charge agreement in status created. */
fun newEvChargeAgreement(code: String, title: String): EvChargeAgreement {
    return EvChargeAgreement(
        code = code,
        title = title,
        batchSize = 0,
        score = 0.0,
        lane = "",
        trailerId = "",
        visible = false,
        phone = "",
        startsAt = 0,
        checksum = "",
        status = EvChargeAgreementStatus.CREATED,
    )
}
