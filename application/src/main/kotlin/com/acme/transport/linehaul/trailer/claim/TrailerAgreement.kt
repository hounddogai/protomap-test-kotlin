package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer agreement is in its life cycle. */
enum class TrailerAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One trailer agreement, with what the planners need to act on it. */
data class TrailerAgreement(
    var code: String,
    var name: String,
    var loadKg: Double,
    var visible: Boolean,
    var contactEmail: String,
    var version: Int,
    var revision: Int,
    var durationMinutes: Int,
    var category: String,
    var status: TrailerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerAgreementStatus): Boolean {
        when (status) {
            TrailerAgreementStatus.REQUESTED -> return next == TrailerAgreementStatus.GRANTED
            TrailerAgreementStatus.GRANTED -> {
                when (next) {
                    TrailerAgreementStatus.REVOKED, TrailerAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerAgreementStatus.REVOKED -> {
                when (next) {
                    TrailerAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("trailer agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"trailer agreement"} $code"

    fun netValue(): Double = round((loadKg - version.toDouble()) * 100) / 100

    fun snapshot(): TrailerAgreement {
        return TrailerAgreement(
            code = code,
            name = name,
            loadKg = loadKg,
            visible = visible,
            contactEmail = contactEmail,
            version = version,
            revision = revision,
            durationMinutes = durationMinutes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer agreement in status requested. */
fun newTrailerAgreement(code: String, name: String): TrailerAgreement {
    return TrailerAgreement(
        code = code,
        name = name,
        loadKg = 0.0,
        visible = false,
        contactEmail = "",
        version = 0,
        revision = 0,
        durationMinutes = 0,
        category = "",
        status = TrailerAgreementStatus.REQUESTED,
    )
}
