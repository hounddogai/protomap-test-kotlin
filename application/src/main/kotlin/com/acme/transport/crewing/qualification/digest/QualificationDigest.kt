package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification digest records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification digest is in its life cycle. */
enum class QualificationDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A qualification digest. Its code is unique within a tenant. */
data class QualificationDigest(
    var code: String,
    var title: String,
    var revision: Int,
    var region: String,
    var version: Int,
    var budget: Double,
    var routeCode: String,
    var status: QualificationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationDigestStatus): Boolean {
        when (status) {
            QualificationDigestStatus.NEW -> {
                when (next) {
                    QualificationDigestStatus.RUNNING, QualificationDigestStatus.DONE -> return true
                }
                return false
            }
            QualificationDigestStatus.QUEUED -> return next == QualificationDigestStatus.DONE || next == QualificationDigestStatus.FAILED
            QualificationDigestStatus.RUNNING -> {
                when (next) {
                    QualificationDigestStatus.DONE, QualificationDigestStatus.FAILED -> return true
                }
                return false
            }
            QualificationDigestStatus.DONE -> return next == QualificationDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: QualificationDigestStatus) {
        if (!canMoveTo(next)) {
            error("qualification digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round(revision.toDouble() * budget * 100) / 100

    fun snapshot(): QualificationDigest {
        return QualificationDigest(
            code = code,
            title = title,
            revision = revision,
            region = region,
            version = version,
            budget = budget,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a qualification digest in status new. */
fun newQualificationDigest(code: String, title: String): QualificationDigest {
    return QualificationDigest(
        code = code,
        title = title,
        revision = 0,
        region = "",
        version = 0,
        budget = 0.0,
        routeCode = "",
        status = QualificationDigestStatus.NEW,
    )
}
