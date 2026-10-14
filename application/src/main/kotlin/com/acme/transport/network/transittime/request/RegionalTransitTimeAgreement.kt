package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.normalize

/** Where a regional transit time agreement is in its life cycle. */
enum class RegionalTransitTimeAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional transit time agreement and its current status. */
data class RegionalTransitTimeAgreement(
    var code: String,
    var name: String,
    var trailerId: String,
    var margin: Double,
    var visible: Boolean,
    var reviewer: String,
    var status: RegionalTransitTimeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTransitTimeAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTransitTimeAgreementStatus): Boolean {
        when (status) {
            RegionalTransitTimeAgreementStatus.NEW -> {
                when (next) {
                    RegionalTransitTimeAgreementStatus.RUNNING, RegionalTransitTimeAgreementStatus.DONE -> return true
                }
                return false
            }
            RegionalTransitTimeAgreementStatus.QUEUED -> {
                when (next) {
                    RegionalTransitTimeAgreementStatus.DONE -> return true
                }
                return false
            }
            RegionalTransitTimeAgreementStatus.RUNNING -> return next == RegionalTransitTimeAgreementStatus.FAILED
            RegionalTransitTimeAgreementStatus.DONE -> {
                when (next) {
                    RegionalTransitTimeAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTransitTimeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional transit time agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional transit time agreement"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): RegionalTransitTimeAgreement {
        return RegionalTransitTimeAgreement(
            code = code,
            name = name,
            trailerId = trailerId,
            margin = margin,
            visible = visible,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional transit time agreement in status new. */
fun newRegionalTransitTimeAgreement(code: String, name: String): RegionalTransitTimeAgreement {
    return RegionalTransitTimeAgreement(
        code = code,
        name = name,
        trailerId = "",
        margin = 0.0,
        visible = false,
        reviewer = "",
        status = RegionalTransitTimeAgreementStatus.NEW,
    )
}
