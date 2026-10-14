package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service area agreement is in its life cycle. */
enum class WeightedServiceAreaAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted service area agreement and its current status. */
data class WeightedServiceAreaAgreement(
    var code: String,
    var name: String,
    var windowDays: Int,
    var ceiling: Double,
    var revision: Int,
    var locale: String,
    var status: WeightedServiceAreaAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServiceAreaAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServiceAreaAgreementStatus): Boolean {
        when (status) {
            WeightedServiceAreaAgreementStatus.PROPOSED -> {
                when (next) {
                    WeightedServiceAreaAgreementStatus.REJECTED, WeightedServiceAreaAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedServiceAreaAgreementStatus.APPROVED -> {
                when (next) {
                    WeightedServiceAreaAgreementStatus.REJECTED, WeightedServiceAreaAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedServiceAreaAgreementStatus.REJECTED -> return next == WeightedServiceAreaAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedServiceAreaAgreementStatus) {
        if (!canMoveTo(next)) {
            error("weighted service area agreement $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(windowDays.toDouble() * ceiling * 100) / 100

    fun snapshot(): WeightedServiceAreaAgreement {
        return WeightedServiceAreaAgreement(
            code = code,
            name = name,
            windowDays = windowDays,
            ceiling = ceiling,
            revision = revision,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted service area agreement in status proposed. */
fun newWeightedServiceAreaAgreement(code: String, name: String): WeightedServiceAreaAgreement {
    return WeightedServiceAreaAgreement(
        code = code,
        name = name,
        windowDays = 0,
        ceiling = 0.0,
        revision = 0,
        locale = "",
        status = WeightedServiceAreaAgreementStatus.PROPOSED,
    )
}
