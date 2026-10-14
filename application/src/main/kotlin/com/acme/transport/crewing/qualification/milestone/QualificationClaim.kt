package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification claim is in its life cycle. */
enum class QualificationClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification claim as the qualification team keeps it. */
data class QualificationClaim(
    var code: String,
    var title: String,
    var rate: Double,
    var vehicleId: String,
    var approved: Boolean,
    var notes: String,
    var batchSize: Int,
    var score: Double,
    var status: QualificationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationClaimStatus): Boolean {
        when (status) {
            QualificationClaimStatus.CREATED -> return next == QualificationClaimStatus.VALIDATED
            QualificationClaimStatus.VALIDATED -> {
                when (next) {
                    QualificationClaimStatus.PUBLISHED, QualificationClaimStatus.RETIRED -> return true
                }
                return false
            }
            QualificationClaimStatus.PUBLISHED -> {
                when (next) {
                    QualificationClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationClaimStatus) {
        if (!canMoveTo(next)) {
            error("qualification claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round(batchSize.toDouble() * rate * 100) / 100

    fun snapshot(): QualificationClaim {
        return QualificationClaim(
            code = code,
            title = title,
            rate = rate,
            vehicleId = vehicleId,
            approved = approved,
            notes = notes,
            batchSize = batchSize,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a qualification claim in status created. */
fun newQualificationClaim(code: String, title: String): QualificationClaim {
    return QualificationClaim(
        code = code,
        title = title,
        rate = 0.0,
        vehicleId = "",
        approved = false,
        notes = "",
        batchSize = 0,
        score = 0.0,
        status = QualificationClaimStatus.CREATED,
    )
}
