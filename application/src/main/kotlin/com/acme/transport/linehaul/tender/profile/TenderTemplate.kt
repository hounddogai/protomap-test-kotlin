package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender template is in its life cycle. */
enum class TenderTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tender template. Its code is unique within a tenant. */
data class TenderTemplate(
    var code: String,
    var name: String,
    var locale: String,
    var expiresAt: Int,
    var archived: Boolean,
    var odometer: Int,
    var attempts: Int,
    var lane: String,
    var batchSize: Int,
    var status: TenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderTemplateStatus): Boolean {
        when (status) {
            TenderTemplateStatus.PROPOSED -> {
                when (next) {
                    TenderTemplateStatus.REJECTED, TenderTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            TenderTemplateStatus.APPROVED -> return next == TenderTemplateStatus.REJECTED || next == TenderTemplateStatus.WITHDRAWN
            TenderTemplateStatus.REJECTED -> return next == TenderTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tender template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((batchSize.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): TenderTemplate {
        return TenderTemplate(
            code = code,
            name = name,
            locale = locale,
            expiresAt = expiresAt,
            archived = archived,
            odometer = odometer,
            attempts = attempts,
            lane = lane,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tender template in status proposed. */
fun newTenderTemplate(code: String, name: String): TenderTemplate {
    return TenderTemplate(
        code = code,
        name = name,
        locale = "",
        expiresAt = 0,
        archived = false,
        odometer = 0,
        attempts = 0,
        lane = "",
        batchSize = 0,
        status = TenderTemplateStatus.PROPOSED,
    )
}
