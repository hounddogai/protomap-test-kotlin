package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell agreement is in its life cycle. */
enum class DwellAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dwell agreement as the dwell team keeps it. */
data class DwellAgreement(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var reviewer: String,
    var lane: String,
    var loadKg: Double,
    var contactEmail: String,
    var description: String,
    var limit: Int,
    var status: DwellAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellAgreementStatus): Boolean {
        when (status) {
            DwellAgreementStatus.NEW -> {
                when (next) {
                    DwellAgreementStatus.FAILED -> return true
                }
                return false
            }
            DwellAgreementStatus.QUEUED -> return next == DwellAgreementStatus.DONE
            DwellAgreementStatus.RUNNING -> {
                when (next) {
                    DwellAgreementStatus.FAILED -> return true
                }
                return false
            }
            DwellAgreementStatus.DONE -> return next == DwellAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DwellAgreementStatus) {
        if (!canMoveTo(next)) {
            error("dwell agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(loadKg * expiresAt.toDouble() * 100) / 100

    fun snapshot(): DwellAgreement {
        return DwellAgreement(
            code = code,
            name = name,
            expiresAt = expiresAt,
            reviewer = reviewer,
            lane = lane,
            loadKg = loadKg,
            contactEmail = contactEmail,
            description = description,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dwell agreement in status new. */
fun newDwellAgreement(code: String, name: String): DwellAgreement {
    return DwellAgreement(
        code = code,
        name = name,
        expiresAt = 0,
        reviewer = "",
        lane = "",
        loadKg = 0.0,
        contactEmail = "",
        description = "",
        limit = 0,
        status = DwellAgreementStatus.NEW,
    )
}
