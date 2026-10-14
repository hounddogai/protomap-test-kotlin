package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit segment is in its life cycle. */
enum class AuditSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A audit segment. Its code is unique within a tenant. */
data class AuditSegment(
    var code: String,
    var title: String,
    var retries: Int,
    var description: String,
    var limit: Int,
    var status: AuditSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditSegmentStatus): Boolean {
        when (status) {
            AuditSegmentStatus.CREATED -> return next == AuditSegmentStatus.PUBLISHED || next == AuditSegmentStatus.RETIRED
            AuditSegmentStatus.VALIDATED -> {
                when (next) {
                    AuditSegmentStatus.RETIRED -> return true
                }
                return false
            }
            AuditSegmentStatus.PUBLISHED -> {
                when (next) {
                    AuditSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditSegmentStatus) {
        if (!canMoveTo(next)) {
            error("audit segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round((limit.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): AuditSegment {
        return AuditSegment(
            code = code,
            title = title,
            retries = retries,
            description = description,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit segment in status created. */
fun newAuditSegment(code: String, title: String): AuditSegment {
    return AuditSegment(
        code = code,
        title = title,
        retries = 0,
        description = "",
        limit = 0,
        status = AuditSegmentStatus.CREATED,
    )
}
