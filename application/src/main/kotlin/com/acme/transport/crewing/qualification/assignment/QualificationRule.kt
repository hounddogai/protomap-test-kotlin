package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification rule is in its life cycle. */
enum class QualificationRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A qualification rule as the qualification team keeps it. */
data class QualificationRule(
    var code: String,
    var title: String,
    var notes: String,
    var region: String,
    var driverId: String,
    var pinned: Boolean,
    var summary: String,
    var startsAt: Int,
    var ratio: Double,
    var ceiling: Double,
    var checksum: String,
    var status: QualificationRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationRuleStatus): Boolean {
        when (status) {
            QualificationRuleStatus.PLANNED -> {
                when (next) {
                    QualificationRuleStatus.SCHEDULED -> return true
                }
                return false
            }
            QualificationRuleStatus.SCHEDULED -> return next == QualificationRuleStatus.IN_PROGRESS || next == QualificationRuleStatus.COMPLETED
            QualificationRuleStatus.IN_PROGRESS -> return next == QualificationRuleStatus.COMPLETED
            QualificationRuleStatus.COMPLETED -> {
                when (next) {
                    QualificationRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationRuleStatus) {
        if (!canMoveTo(next)) {
            error("qualification rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round(startsAt.toDouble() * ratio * 100) / 100

    fun snapshot(): QualificationRule {
        return QualificationRule(
            code = code,
            title = title,
            notes = notes,
            region = region,
            driverId = driverId,
            pinned = pinned,
            summary = summary,
            startsAt = startsAt,
            ratio = ratio,
            ceiling = ceiling,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a qualification rule in status planned. */
fun newQualificationRule(code: String, title: String): QualificationRule {
    return QualificationRule(
        code = code,
        title = title,
        notes = "",
        region = "",
        driverId = "",
        pinned = false,
        summary = "",
        startsAt = 0,
        ratio = 0.0,
        ceiling = 0.0,
        checksum = "",
        status = QualificationRuleStatus.PLANNED,
    )
}
