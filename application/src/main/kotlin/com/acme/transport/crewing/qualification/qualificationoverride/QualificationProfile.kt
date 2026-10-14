package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification profile is in its life cycle. */
enum class QualificationProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification profile as the qualification team keeps it. */
data class QualificationProfile(
    var code: String,
    var name: String,
    var stopCount: Int,
    var country: String,
    var rate: Double,
    var phone: String,
    var score: Double,
    var reviewer: String,
    var status: QualificationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationProfileStatus): Boolean {
        when (status) {
            QualificationProfileStatus.CREATED -> return next == QualificationProfileStatus.RETIRED
            QualificationProfileStatus.VALIDATED -> return next == QualificationProfileStatus.PUBLISHED || next == QualificationProfileStatus.RETIRED
            QualificationProfileStatus.PUBLISHED -> return next == QualificationProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationProfileStatus) {
        if (!canMoveTo(next)) {
            error("qualification profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"qualification profile"} $code"

    fun projectedValue(): Double = round((score - rate) * 100) / 100

    fun snapshot(): QualificationProfile {
        return QualificationProfile(
            code = code,
            name = name,
            stopCount = stopCount,
            country = country,
            rate = rate,
            phone = phone,
            score = score,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a qualification profile in status created. */
fun newQualificationProfile(code: String, name: String): QualificationProfile {
    return QualificationProfile(
        code = code,
        name = name,
        stopCount = 0,
        country = "",
        rate = 0.0,
        phone = "",
        score = 0.0,
        reviewer = "",
        status = QualificationProfileStatus.CREATED,
    )
}
