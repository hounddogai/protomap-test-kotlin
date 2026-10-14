package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default route offset agreement is in its life cycle. */
enum class DefaultRouteOffsetAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default route offset agreement. Its code is unique within a tenant. */
data class DefaultRouteOffsetAgreement(
    var code: String,
    var title: String,
    var driverId: String,
    var rate: Double,
    var notes: String,
    var routeCode: String,
    var currency: String,
    var summary: String,
    var contactEmail: String,
    var shiftCode: String,
    var startsAt: Int,
    var status: DefaultRouteOffsetAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRouteOffsetAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRouteOffsetAgreementStatus): Boolean {
        when (status) {
            DefaultRouteOffsetAgreementStatus.NEW -> return next == DefaultRouteOffsetAgreementStatus.DONE
            DefaultRouteOffsetAgreementStatus.QUEUED -> return next == DefaultRouteOffsetAgreementStatus.RUNNING || next == DefaultRouteOffsetAgreementStatus.FAILED
            DefaultRouteOffsetAgreementStatus.RUNNING -> return next == DefaultRouteOffsetAgreementStatus.DONE || next == DefaultRouteOffsetAgreementStatus.FAILED
            DefaultRouteOffsetAgreementStatus.DONE -> return next == DefaultRouteOffsetAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultRouteOffsetAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default route offset agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default route offset agreement"} $code"

    fun netTotal(): Double = round((startsAt.toDouble() - rate) * 100) / 100

    fun snapshot(): DefaultRouteOffsetAgreement {
        return DefaultRouteOffsetAgreement(
            code = code,
            title = title,
            driverId = driverId,
            rate = rate,
            notes = notes,
            routeCode = routeCode,
            currency = currency,
            summary = summary,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default route offset agreement in status new. */
fun newDefaultRouteOffsetAgreement(code: String, title: String): DefaultRouteOffsetAgreement {
    return DefaultRouteOffsetAgreement(
        code = code,
        title = title,
        driverId = "",
        rate = 0.0,
        notes = "",
        routeCode = "",
        currency = "",
        summary = "",
        contactEmail = "",
        shiftCode = "",
        startsAt = 0,
        status = DefaultRouteOffsetAgreementStatus.NEW,
    )
}
