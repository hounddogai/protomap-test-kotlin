package com.acme.transport.network.hub.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub draft records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub draft is in its life cycle. */
enum class HubDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hub draft. Its code is unique within a tenant. */
data class HubDraft(
    var code: String,
    var name: String,
    var externalRef: String,
    var discount: Double,
    var quantity: Int,
    var status: HubDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubDraftStatus): Boolean {
        when (status) {
            HubDraftStatus.NEW -> return next == HubDraftStatus.QUEUED || next == HubDraftStatus.RUNNING
            HubDraftStatus.QUEUED -> return next == HubDraftStatus.RUNNING || next == HubDraftStatus.DONE
            HubDraftStatus.RUNNING -> return next == HubDraftStatus.FAILED
            HubDraftStatus.DONE -> {
                when (next) {
                    HubDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubDraftStatus) {
        if (!canMoveTo(next)) {
            error("hub draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hub draft"} $code"

    fun adjustedValue(): Double = round((discount + quantity.toDouble()) * 100) / 100

    fun snapshot(): HubDraft {
        return HubDraft(
            code = code,
            name = name,
            externalRef = externalRef,
            discount = discount,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub draft in status new. */
fun newHubDraft(code: String, name: String): HubDraft {
    return HubDraft(
        code = code,
        name = name,
        externalRef = "",
        discount = 0.0,
        quantity = 0,
        status = HubDraftStatus.NEW,
    )
}
