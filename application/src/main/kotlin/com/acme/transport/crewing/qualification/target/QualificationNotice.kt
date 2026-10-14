package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification notice is in its life cycle. */
enum class QualificationNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A qualification notice as the qualification team keeps it. */
data class QualificationNotice(
    var code: String,
    var label: String,
    var loadKg: Double,
    var description: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var margin: Double,
    var priority: Int,
    var category: String,
    var trailerId: String,
    var region: String,
    var status: QualificationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationNoticeStatus): Boolean {
        when (status) {
            QualificationNoticeStatus.PLANNED -> {
                when (next) {
                    QualificationNoticeStatus.COMPLETED, QualificationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            QualificationNoticeStatus.SCHEDULED -> return next == QualificationNoticeStatus.IN_PROGRESS || next == QualificationNoticeStatus.CANCELLED
            QualificationNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    QualificationNoticeStatus.COMPLETED, QualificationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            QualificationNoticeStatus.COMPLETED -> {
                when (next) {
                    QualificationNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("qualification notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"qualification notice"} $code"

    fun netValue(): Double = round((durationMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): QualificationNotice {
        return QualificationNotice(
            code = code,
            label = label,
            loadKg = loadKg,
            description = description,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            margin = margin,
            priority = priority,
            category = category,
            trailerId = trailerId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a qualification notice in status planned. */
fun newQualificationNotice(code: String, label: String): QualificationNotice {
    return QualificationNotice(
        code = code,
        label = label,
        loadKg = 0.0,
        description = "",
        durationMinutes = 0,
        distanceKm = 0.0,
        margin = 0.0,
        priority = 0,
        category = "",
        trailerId = "",
        region = "",
        status = QualificationNoticeStatus.PLANNED,
    )
}
