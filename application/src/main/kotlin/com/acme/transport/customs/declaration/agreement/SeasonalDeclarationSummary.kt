package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal declaration summary is in its life cycle. */
enum class SeasonalDeclarationSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal declaration summary as the declaration team keeps it. */
data class SeasonalDeclarationSummary(
    var code: String,
    var title: String,
    var discount: Double,
    var cost: Double,
    var endsAt: Int,
    var quantity: Int,
    var status: SeasonalDeclarationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationSummaryStatus): Boolean {
        when (status) {
            SeasonalDeclarationSummaryStatus.PROPOSED -> return next == SeasonalDeclarationSummaryStatus.APPROVED
            SeasonalDeclarationSummaryStatus.APPROVED -> return next == SeasonalDeclarationSummaryStatus.REJECTED || next == SeasonalDeclarationSummaryStatus.WITHDRAWN
            SeasonalDeclarationSummaryStatus.REJECTED -> {
                when (next) {
                    SeasonalDeclarationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal declaration summary"} $code"

    fun netExposure(): Double = round((cost + quantity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDeclarationSummary {
        return SeasonalDeclarationSummary(
            code = code,
            title = title,
            discount = discount,
            cost = cost,
            endsAt = endsAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal declaration summary in status proposed. */
fun newSeasonalDeclarationSummary(code: String, title: String): SeasonalDeclarationSummary {
    return SeasonalDeclarationSummary(
        code = code,
        title = title,
        discount = 0.0,
        cost = 0.0,
        endsAt = 0,
        quantity = 0,
        status = SeasonalDeclarationSummaryStatus.PROPOSED,
    )
}
