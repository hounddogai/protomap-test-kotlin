package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification quota is in its life cycle. */
enum class QualificationQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A qualification quota as the qualification team keeps it. */
data class QualificationQuota(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var checksum: String,
    var threshold: Double,
    var minimum: Double,
    var dock: String,
    var lane: String,
    var region: String,
    var archived: Boolean,
    var status: QualificationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationQuotaStatus): Boolean {
        when (status) {
            QualificationQuotaStatus.PROPOSED -> {
                when (next) {
                    QualificationQuotaStatus.REJECTED, QualificationQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            QualificationQuotaStatus.APPROVED -> {
                when (next) {
                    QualificationQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            QualificationQuotaStatus.REJECTED -> return next == QualificationQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: QualificationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("qualification quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"qualification quota"} $code"

    fun weightedLoad(): Double = round(minimum * expiresAt.toDouble() * 100) / 100

    fun snapshot(): QualificationQuota {
        return QualificationQuota(
            code = code,
            label = label,
            expiresAt = expiresAt,
            checksum = checksum,
            threshold = threshold,
            minimum = minimum,
            dock = dock,
            lane = lane,
            region = region,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification quota in status proposed. */
fun newQualificationQuota(code: String, label: String): QualificationQuota {
    return QualificationQuota(
        code = code,
        label = label,
        expiresAt = 0,
        checksum = "",
        threshold = 0.0,
        minimum = 0.0,
        dock = "",
        lane = "",
        region = "",
        archived = false,
        status = QualificationQuotaStatus.PROPOSED,
    )
}
