package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive drop record is in its life cycle. */
enum class AdaptiveDropRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive drop record and its current status. */
data class AdaptiveDropRecord(
    var code: String,
    var label: String,
    var country: String,
    var approved: Boolean,
    var score: Double,
    var routeCode: String,
    var limit: Int,
    var contactEmail: String,
    var status: AdaptiveDropRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDropRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDropRecordStatus): Boolean {
        when (status) {
            AdaptiveDropRecordStatus.NEW -> return next == AdaptiveDropRecordStatus.DONE
            AdaptiveDropRecordStatus.QUEUED -> {
                when (next) {
                    AdaptiveDropRecordStatus.RUNNING, AdaptiveDropRecordStatus.DONE -> return true
                }
                return false
            }
            AdaptiveDropRecordStatus.RUNNING -> {
                when (next) {
                    AdaptiveDropRecordStatus.DONE, AdaptiveDropRecordStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveDropRecordStatus.DONE -> return next == AdaptiveDropRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveDropRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive drop record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round(score * limit.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDropRecord {
        return AdaptiveDropRecord(
            code = code,
            label = label,
            country = country,
            approved = approved,
            score = score,
            routeCode = routeCode,
            limit = limit,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive drop record in status new. */
fun newAdaptiveDropRecord(code: String, label: String): AdaptiveDropRecord {
    return AdaptiveDropRecord(
        code = code,
        label = label,
        country = "",
        approved = false,
        score = 0.0,
        routeCode = "",
        limit = 0,
        contactEmail = "",
        status = AdaptiveDropRecordStatus.NEW,
    )
}
