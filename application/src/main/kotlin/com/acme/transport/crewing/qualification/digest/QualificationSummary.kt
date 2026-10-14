package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification summary is in its life cycle. */
enum class QualificationSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A qualification summary. Its code is unique within a tenant. */
data class QualificationSummary(
    var code: String,
    var name: String,
    var endsAt: Int,
    var budget: Double,
    var ownerId: String,
    var status: QualificationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationSummaryStatus): Boolean {
        when (status) {
            QualificationSummaryStatus.REQUESTED -> return next == QualificationSummaryStatus.GRANTED || next == QualificationSummaryStatus.EXPIRED
            QualificationSummaryStatus.GRANTED -> {
                when (next) {
                    QualificationSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationSummaryStatus.REVOKED -> {
                when (next) {
                    QualificationSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("qualification summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((endsAt.toDouble() + budget) * 100) / 100

    fun snapshot(): QualificationSummary {
        return QualificationSummary(
            code = code,
            name = name,
            endsAt = endsAt,
            budget = budget,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification summary in status requested. */
fun newQualificationSummary(code: String, name: String): QualificationSummary {
    return QualificationSummary(
        code = code,
        name = name,
        endsAt = 0,
        budget = 0.0,
        ownerId = "",
        status = QualificationSummaryStatus.REQUESTED,
    )
}
