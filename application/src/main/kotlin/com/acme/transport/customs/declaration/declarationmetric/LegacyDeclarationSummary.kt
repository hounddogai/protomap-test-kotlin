package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.text.normalize

/** Where a legacy declaration summary is in its life cycle. */
enum class LegacyDeclarationSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy declaration summary and its current status. */
data class LegacyDeclarationSummary(
    var code: String,
    var name: String,
    var cost: Double,
    var phone: String,
    var notes: String,
    var vehicleId: String,
    var contactEmail: String,
    var channel: String,
    var status: LegacyDeclarationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationSummaryStatus): Boolean {
        when (status) {
            LegacyDeclarationSummaryStatus.CREATED -> {
                when (next) {
                    LegacyDeclarationSummaryStatus.VALIDATED, LegacyDeclarationSummaryStatus.RETIRED -> return true
                }
                return false
            }
            LegacyDeclarationSummaryStatus.VALIDATED -> {
                when (next) {
                    LegacyDeclarationSummaryStatus.PUBLISHED, LegacyDeclarationSummaryStatus.RETIRED -> return true
                }
                return false
            }
            LegacyDeclarationSummaryStatus.PUBLISHED -> {
                when (next) {
                    LegacyDeclarationSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = cost

    fun snapshot(): LegacyDeclarationSummary {
        return LegacyDeclarationSummary(
            code = code,
            name = name,
            cost = cost,
            phone = phone,
            notes = notes,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy declaration summary in status created. */
fun newLegacyDeclarationSummary(code: String, name: String): LegacyDeclarationSummary {
    return LegacyDeclarationSummary(
        code = code,
        name = name,
        cost = 0.0,
        phone = "",
        notes = "",
        vehicleId = "",
        contactEmail = "",
        channel = "",
        status = LegacyDeclarationSummaryStatus.CREATED,
    )
}
