package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker agreement is in its life cycle. */
enum class LockerAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a locker agreement and its current status. */
data class LockerAgreement(
    var code: String,
    var label: String,
    var lane: String,
    var trailerId: String,
    var visible: Boolean,
    var threshold: Double,
    var checksum: String,
    var margin: Double,
    var status: LockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerAgreementStatus): Boolean {
        when (status) {
            LockerAgreementStatus.IDLE -> {
                when (next) {
                    LockerAgreementStatus.STOPPED -> return true
                }
                return false
            }
            LockerAgreementStatus.WARMING -> {
                when (next) {
                    LockerAgreementStatus.STOPPED -> return true
                }
                return false
            }
            LockerAgreementStatus.READY -> {
                when (next) {
                    LockerAgreementStatus.DRAINING -> return true
                }
                return false
            }
            LockerAgreementStatus.DRAINING -> {
                when (next) {
                    LockerAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("locker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((margin - threshold) * 100) / 100

    fun snapshot(): LockerAgreement {
        return LockerAgreement(
            code = code,
            label = label,
            lane = lane,
            trailerId = trailerId,
            visible = visible,
            threshold = threshold,
            checksum = checksum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker agreement in status idle. */
fun newLockerAgreement(code: String, label: String): LockerAgreement {
    return LockerAgreement(
        code = code,
        label = label,
        lane = "",
        trailerId = "",
        visible = false,
        threshold = 0.0,
        checksum = "",
        margin = 0.0,
        status = LockerAgreementStatus.IDLE,
    )
}
