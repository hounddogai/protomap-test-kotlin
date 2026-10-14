package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.normalize

// The tender bundle records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender bundle is in its life cycle. */
enum class TenderBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tender bundle and its current status. */
data class TenderBundle(
    var code: String,
    var title: String,
    var source: String,
    var driverId: String,
    var graceDays: Int,
    var name: String,
    var status: TenderBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderBundleStatus): Boolean {
        when (status) {
            TenderBundleStatus.REQUESTED -> {
                when (next) {
                    TenderBundleStatus.REVOKED, TenderBundleStatus.EXPIRED -> return true
                }
                return false
            }
            TenderBundleStatus.GRANTED -> return next == TenderBundleStatus.EXPIRED
            TenderBundleStatus.REVOKED -> {
                when (next) {
                    TenderBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderBundleStatus) {
        if (!canMoveTo(next)) {
            error("tender bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): TenderBundle {
        return TenderBundle(
            code = code,
            title = title,
            source = source,
            driverId = driverId,
            graceDays = graceDays,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tender bundle in status requested. */
fun newTenderBundle(code: String, title: String): TenderBundle {
    return TenderBundle(
        code = code,
        title = title,
        source = "",
        driverId = "",
        graceDays = 0,
        name = "",
        status = TenderBundleStatus.REQUESTED,
    )
}
