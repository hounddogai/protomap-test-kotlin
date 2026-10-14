package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize

/** Where a tiered trailer yard exception is in its life cycle. */
enum class TieredTrailerYardExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered trailer yard exception as the trailer yard team keeps it. */
data class TieredTrailerYardException(
    var code: String,
    var label: String,
    var phone: String,
    var ownerId: String,
    var dock: String,
    var depot: String,
    var quantity: Int,
    var status: TieredTrailerYardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerYardExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerYardExceptionStatus): Boolean {
        when (status) {
            TieredTrailerYardExceptionStatus.CREATED -> return next == TieredTrailerYardExceptionStatus.RETIRED
            TieredTrailerYardExceptionStatus.VALIDATED -> return next == TieredTrailerYardExceptionStatus.PUBLISHED || next == TieredTrailerYardExceptionStatus.RETIRED
            TieredTrailerYardExceptionStatus.PUBLISHED -> return next == TieredTrailerYardExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredTrailerYardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer yard exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): TieredTrailerYardException {
        return TieredTrailerYardException(
            code = code,
            label = label,
            phone = phone,
            ownerId = ownerId,
            dock = dock,
            depot = depot,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered trailer yard exception in status created. */
fun newTieredTrailerYardException(code: String, label: String): TieredTrailerYardException {
    return TieredTrailerYardException(
        code = code,
        label = label,
        phone = "",
        ownerId = "",
        dock = "",
        depot = "",
        quantity = 0,
        status = TieredTrailerYardExceptionStatus.CREATED,
    )
}
