package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation summary is in its life cycle. */
enum class ViolationSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One violation summary, with what the planners need to act on it. */
data class ViolationSummary(
    var code: String,
    var name: String,
    var category: String,
    var contactEmail: String,
    var expiresAt: Int,
    var version: Int,
    var checksum: String,
    var ownerId: String,
    var updatedAt: Int,
    var endsAt: Int,
    var status: ViolationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationSummaryStatus): Boolean {
        when (status) {
            ViolationSummaryStatus.REQUESTED -> return next == ViolationSummaryStatus.GRANTED || next == ViolationSummaryStatus.REVOKED
            ViolationSummaryStatus.GRANTED -> return next == ViolationSummaryStatus.REVOKED
            ViolationSummaryStatus.REVOKED -> return next == ViolationSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("violation summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((expiresAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ViolationSummary {
        return ViolationSummary(
            code = code,
            name = name,
            category = category,
            contactEmail = contactEmail,
            expiresAt = expiresAt,
            version = version,
            checksum = checksum,
            ownerId = ownerId,
            updatedAt = updatedAt,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a violation summary in status requested. */
fun newViolationSummary(code: String, name: String): ViolationSummary {
    return ViolationSummary(
        code = code,
        name = name,
        category = "",
        contactEmail = "",
        expiresAt = 0,
        version = 0,
        checksum = "",
        ownerId = "",
        updatedAt = 0,
        endsAt = 0,
        status = ViolationSummaryStatus.REQUESTED,
    )
}
