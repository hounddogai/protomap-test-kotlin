package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification agreement is in its life cycle. */
enum class QualificationAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One qualification agreement, with what the planners need to act on it. */
data class QualificationAgreement(
    var code: String,
    var label: String,
    var ratio: Double,
    var checksum: String,
    var discount: Double,
    var dock: String,
    var startsAt: Int,
    var source: String,
    var driverId: String,
    var contactEmail: String,
    var quantity: Int,
    var status: QualificationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationAgreementStatus): Boolean {
        when (status) {
            QualificationAgreementStatus.IDLE -> {
                when (next) {
                    QualificationAgreementStatus.WARMING, QualificationAgreementStatus.STOPPED -> return true
                }
                return false
            }
            QualificationAgreementStatus.WARMING -> {
                when (next) {
                    QualificationAgreementStatus.READY -> return true
                }
                return false
            }
            QualificationAgreementStatus.READY -> return next == QualificationAgreementStatus.DRAINING || next == QualificationAgreementStatus.STOPPED
            QualificationAgreementStatus.DRAINING -> {
                when (next) {
                    QualificationAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("qualification agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(startsAt.toDouble() * discount * 100) / 100

    fun snapshot(): QualificationAgreement {
        return QualificationAgreement(
            code = code,
            label = label,
            ratio = ratio,
            checksum = checksum,
            discount = discount,
            dock = dock,
            startsAt = startsAt,
            source = source,
            driverId = driverId,
            contactEmail = contactEmail,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a qualification agreement in status idle. */
fun newQualificationAgreement(code: String, label: String): QualificationAgreement {
    return QualificationAgreement(
        code = code,
        label = label,
        ratio = 0.0,
        checksum = "",
        discount = 0.0,
        dock = "",
        startsAt = 0,
        source = "",
        driverId = "",
        contactEmail = "",
        quantity = 0,
        status = QualificationAgreementStatus.IDLE,
    )
}
