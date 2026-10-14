package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled declaration baseline is in its life cycle. */
enum class ScheduledDeclarationBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled declaration baseline and its current status. */
data class ScheduledDeclarationBaseline(
    var code: String,
    var name: String,
    var discount: Double,
    var category: String,
    var pallets: Int,
    var contactEmail: String,
    var amount: Double,
    var phone: String,
    var threshold: Double,
    var weight: Double,
    var status: ScheduledDeclarationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDeclarationBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDeclarationBaselineStatus): Boolean {
        when (status) {
            ScheduledDeclarationBaselineStatus.CREATED -> {
                when (next) {
                    ScheduledDeclarationBaselineStatus.VALIDATED, ScheduledDeclarationBaselineStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledDeclarationBaselineStatus.VALIDATED -> {
                when (next) {
                    ScheduledDeclarationBaselineStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledDeclarationBaselineStatus.PUBLISHED -> return next == ScheduledDeclarationBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledDeclarationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("scheduled declaration baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round(discount * amount * 100) / 100

    fun snapshot(): ScheduledDeclarationBaseline {
        return ScheduledDeclarationBaseline(
            code = code,
            name = name,
            discount = discount,
            category = category,
            pallets = pallets,
            contactEmail = contactEmail,
            amount = amount,
            phone = phone,
            threshold = threshold,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled declaration baseline in status created. */
fun newScheduledDeclarationBaseline(code: String, name: String): ScheduledDeclarationBaseline {
    return ScheduledDeclarationBaseline(
        code = code,
        name = name,
        discount = 0.0,
        category = "",
        pallets = 0,
        contactEmail = "",
        amount = 0.0,
        phone = "",
        threshold = 0.0,
        weight = 0.0,
        status = ScheduledDeclarationBaselineStatus.CREATED,
    )
}
