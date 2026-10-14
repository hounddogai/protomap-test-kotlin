package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff policy is in its life cycle. */
enum class CutoffPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One cutoff policy, with what the planners need to act on it. */
data class CutoffPolicy(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var currency: String,
    var externalRef: String,
    var phone: String,
    var notes: String,
    var endsAt: Int,
    var quantity: Int,
    var locale: String,
    var status: CutoffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffPolicyStatus): Boolean {
        when (status) {
            CutoffPolicyStatus.CREATED -> {
                when (next) {
                    CutoffPolicyStatus.VALIDATED, CutoffPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            CutoffPolicyStatus.VALIDATED -> {
                when (next) {
                    CutoffPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            CutoffPolicyStatus.PUBLISHED -> return next == CutoffPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CutoffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("cutoff policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((quantity.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): CutoffPolicy {
        return CutoffPolicy(
            code = code,
            title = title,
            pinned = pinned,
            currency = currency,
            externalRef = externalRef,
            phone = phone,
            notes = notes,
            endsAt = endsAt,
            quantity = quantity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a cutoff policy in status created. */
fun newCutoffPolicy(code: String, title: String): CutoffPolicy {
    return CutoffPolicy(
        code = code,
        title = title,
        pinned = false,
        currency = "",
        externalRef = "",
        phone = "",
        notes = "",
        endsAt = 0,
        quantity = 0,
        locale = "",
        status = CutoffPolicyStatus.CREATED,
    )
}
