package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered sortation template is in its life cycle. */
enum class TieredSortationTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered sortation template as the sortation team keeps it. */
data class TieredSortationTemplate(
    var code: String,
    var name: String,
    var rate: Double,
    var discount: Double,
    var score: Double,
    var contactEmail: String,
    var minimum: Double,
    var retries: Int,
    var budget: Double,
    var quantity: Int,
    var segment: String,
    var status: TieredSortationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationTemplateStatus): Boolean {
        when (status) {
            TieredSortationTemplateStatus.DRAFT -> return next == TieredSortationTemplateStatus.SUSPENDED
            TieredSortationTemplateStatus.PENDING -> {
                when (next) {
                    TieredSortationTemplateStatus.ACTIVE, TieredSortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            TieredSortationTemplateStatus.ACTIVE -> {
                when (next) {
                    TieredSortationTemplateStatus.SUSPENDED, TieredSortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            TieredSortationTemplateStatus.SUSPENDED -> {
                when (next) {
                    TieredSortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSortationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((minimum + quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredSortationTemplate {
        return TieredSortationTemplate(
            code = code,
            name = name,
            rate = rate,
            discount = discount,
            score = score,
            contactEmail = contactEmail,
            minimum = minimum,
            retries = retries,
            budget = budget,
            quantity = quantity,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered sortation template in status draft. */
fun newTieredSortationTemplate(code: String, name: String): TieredSortationTemplate {
    return TieredSortationTemplate(
        code = code,
        name = name,
        rate = 0.0,
        discount = 0.0,
        score = 0.0,
        contactEmail = "",
        minimum = 0.0,
        retries = 0,
        budget = 0.0,
        quantity = 0,
        segment = "",
        status = TieredSortationTemplateStatus.DRAFT,
    )
}
