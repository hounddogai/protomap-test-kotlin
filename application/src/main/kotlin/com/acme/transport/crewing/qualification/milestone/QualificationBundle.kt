package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification bundle is in its life cycle. */
enum class QualificationBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A qualification bundle. Its code is unique within a tenant. */
data class QualificationBundle(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var margin: Double,
    var segment: String,
    var contactEmail: String,
    var notes: String,
    var loadKg: Double,
    var status: QualificationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationBundleStatus): Boolean {
        when (status) {
            QualificationBundleStatus.REQUESTED -> {
                when (next) {
                    QualificationBundleStatus.REVOKED, QualificationBundleStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationBundleStatus.GRANTED -> {
                when (next) {
                    QualificationBundleStatus.REVOKED, QualificationBundleStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationBundleStatus.REVOKED -> return next == QualificationBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: QualificationBundleStatus) {
        if (!canMoveTo(next)) {
            error("qualification bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((updatedAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): QualificationBundle {
        return QualificationBundle(
            code = code,
            name = name,
            updatedAt = updatedAt,
            margin = margin,
            segment = segment,
            contactEmail = contactEmail,
            notes = notes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a qualification bundle in status requested. */
fun newQualificationBundle(code: String, name: String): QualificationBundle {
    return QualificationBundle(
        code = code,
        name = name,
        updatedAt = 0,
        margin = 0.0,
        segment = "",
        contactEmail = "",
        notes = "",
        loadKg = 0.0,
        status = QualificationBundleStatus.REQUESTED,
    )
}
