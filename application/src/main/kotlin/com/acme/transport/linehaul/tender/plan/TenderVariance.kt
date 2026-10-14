package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender variance is in its life cycle. */
enum class TenderVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tender variance as the tender team keeps it. */
data class TenderVariance(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var phone: String,
    var revision: Int,
    var sequence: Int,
    var archived: Boolean,
    var source: String,
    var windowDays: Int,
    var amount: Double,
    var status: TenderVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderVarianceStatus): Boolean {
        when (status) {
            TenderVarianceStatus.PLANNED -> return next == TenderVarianceStatus.IN_PROGRESS || next == TenderVarianceStatus.CANCELLED
            TenderVarianceStatus.SCHEDULED -> return next == TenderVarianceStatus.IN_PROGRESS || next == TenderVarianceStatus.COMPLETED
            TenderVarianceStatus.IN_PROGRESS -> return next == TenderVarianceStatus.COMPLETED || next == TenderVarianceStatus.CANCELLED
            TenderVarianceStatus.COMPLETED -> {
                when (next) {
                    TenderVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tender variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(revision.toDouble() * amount * 100) / 100

    fun snapshot(): TenderVariance {
        return TenderVariance(
            code = code,
            title = title,
            attributes = attributes,
            quantity = quantity,
            phone = phone,
            revision = revision,
            sequence = sequence,
            archived = archived,
            source = source,
            windowDays = windowDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tender variance in status planned. */
fun newTenderVariance(code: String, title: String): TenderVariance {
    return TenderVariance(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        phone = "",
        revision = 0,
        sequence = 0,
        archived = false,
        source = "",
        windowDays = 0,
        amount = 0.0,
        status = TenderVarianceStatus.PLANNED,
    )
}
