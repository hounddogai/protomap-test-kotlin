package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff agreement is in its life cycle. */
enum class CutoffAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff agreement. Its code is unique within a tenant. */
data class CutoffAgreement(
    var code: String,
    var title: String,
    var region: String,
    var version: Int,
    var minimum: Double,
    var ratio: Double,
    var status: CutoffAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffAgreementStatus): Boolean {
        when (status) {
            CutoffAgreementStatus.NEW -> return next == CutoffAgreementStatus.RUNNING || next == CutoffAgreementStatus.DONE
            CutoffAgreementStatus.QUEUED -> return next == CutoffAgreementStatus.RUNNING
            CutoffAgreementStatus.RUNNING -> {
                when (next) {
                    CutoffAgreementStatus.DONE, CutoffAgreementStatus.FAILED -> return true
                }
                return false
            }
            CutoffAgreementStatus.DONE -> return next == CutoffAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffAgreementStatus) {
        if (!canMoveTo(next)) {
            error("cutoff agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round(minimum * ratio * 100) / 100

    fun snapshot(): CutoffAgreement {
        return CutoffAgreement(
            code = code,
            title = title,
            region = region,
            version = version,
            minimum = minimum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a cutoff agreement in status new. */
fun newCutoffAgreement(code: String, title: String): CutoffAgreement {
    return CutoffAgreement(
        code = code,
        title = title,
        region = "",
        version = 0,
        minimum = 0.0,
        ratio = 0.0,
        status = CutoffAgreementStatus.NEW,
    )
}
