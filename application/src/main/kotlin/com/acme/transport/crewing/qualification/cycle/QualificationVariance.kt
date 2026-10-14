package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification variance is in its life cycle. */
enum class QualificationVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a qualification variance and its current status. */
data class QualificationVariance(
    var code: String,
    var name: String,
    var approved: Boolean,
    var checksum: String,
    var amount: Double,
    var endsAt: Int,
    var fuelLiters: Double,
    var externalRef: String,
    var reviewer: String,
    var version: Int,
    var locale: String,
    var status: QualificationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationVarianceStatus): Boolean {
        when (status) {
            QualificationVarianceStatus.OPEN -> return next == QualificationVarianceStatus.RESOLVED
            QualificationVarianceStatus.IN_REVIEW -> return next == QualificationVarianceStatus.RESOLVED || next == QualificationVarianceStatus.ARCHIVED
            QualificationVarianceStatus.RESOLVED -> return next == QualificationVarianceStatus.REOPENED || next == QualificationVarianceStatus.ARCHIVED
            QualificationVarianceStatus.REOPENED -> return next == QualificationVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("qualification variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"qualification variance"} $code"

    fun adjustedLoad(): Double = round(fuelLiters * amount * 100) / 100

    fun snapshot(): QualificationVariance {
        return QualificationVariance(
            code = code,
            name = name,
            approved = approved,
            checksum = checksum,
            amount = amount,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            reviewer = reviewer,
            version = version,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a qualification variance in status open. */
fun newQualificationVariance(code: String, name: String): QualificationVariance {
    return QualificationVariance(
        code = code,
        name = name,
        approved = false,
        checksum = "",
        amount = 0.0,
        endsAt = 0,
        fuelLiters = 0.0,
        externalRef = "",
        reviewer = "",
        version = 0,
        locale = "",
        status = QualificationVarianceStatus.OPEN,
    )
}
