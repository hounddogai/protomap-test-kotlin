package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hub draft is in its life cycle. */
enum class CappedHubDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped hub draft. Its code is unique within a tenant. */
data class CappedHubDraft(
    var code: String,
    var label: String,
    var timezone: String,
    var quantity: Int,
    var locale: String,
    var odometer: Int,
    var status: CappedHubDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHubDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHubDraftStatus): Boolean {
        when (status) {
            CappedHubDraftStatus.DRAFT -> {
                when (next) {
                    CappedHubDraftStatus.CLOSED -> return true
                }
                return false
            }
            CappedHubDraftStatus.PENDING -> {
                when (next) {
                    CappedHubDraftStatus.ACTIVE -> return true
                }
                return false
            }
            CappedHubDraftStatus.ACTIVE -> {
                when (next) {
                    CappedHubDraftStatus.CLOSED -> return true
                }
                return false
            }
            CappedHubDraftStatus.SUSPENDED -> {
                when (next) {
                    CappedHubDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedHubDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped hub draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((quantity.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): CappedHubDraft {
        return CappedHubDraft(
            code = code,
            label = label,
            timezone = timezone,
            quantity = quantity,
            locale = locale,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped hub draft in status draft. */
fun newCappedHubDraft(code: String, label: String): CappedHubDraft {
    return CappedHubDraft(
        code = code,
        label = label,
        timezone = "",
        quantity = 0,
        locale = "",
        odometer = 0,
        status = CappedHubDraftStatus.DRAFT,
    )
}
