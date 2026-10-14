package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender agreement is in its life cycle. */
enum class TenderAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender agreement as the tender team keeps it. */
data class TenderAgreement(
    var code: String,
    var title: String,
    var odometer: Int,
    var tags: MutableList<String>,
    var carrier: String,
    var updatedAt: Int,
    var status: TenderAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderAgreementStatus): Boolean {
        when (status) {
            TenderAgreementStatus.CREATED -> return next == TenderAgreementStatus.PUBLISHED
            TenderAgreementStatus.VALIDATED -> {
                when (next) {
                    TenderAgreementStatus.PUBLISHED -> return true
                }
                return false
            }
            TenderAgreementStatus.PUBLISHED -> {
                when (next) {
                    TenderAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tender agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(odometer.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): TenderAgreement {
        return TenderAgreement(
            code = code,
            title = title,
            odometer = odometer,
            tags = tags,
            carrier = carrier,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tender agreement in status created. */
fun newTenderAgreement(code: String, title: String): TenderAgreement {
    return TenderAgreement(
        code = code,
        title = title,
        odometer = 0,
        tags = mutableListOf<String>(),
        carrier = "",
        updatedAt = 0,
        status = TenderAgreementStatus.CREATED,
    )
}
