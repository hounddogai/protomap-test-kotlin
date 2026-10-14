package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.normalize

/** Where a rolling audit mapping is in its life cycle. */
enum class RollingAuditMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling audit mapping as the audit team keeps it. */
data class RollingAuditMapping(
    var code: String,
    var name: String,
    var archived: Boolean,
    var tags: MutableList<String>,
    var notes: String,
    var threshold: Double,
    var status: RollingAuditMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditMappingStatus): Boolean {
        when (status) {
            RollingAuditMappingStatus.CREATED -> return next == RollingAuditMappingStatus.RETIRED
            RollingAuditMappingStatus.VALIDATED -> {
                when (next) {
                    RollingAuditMappingStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingAuditMappingStatus.PUBLISHED -> {
                when (next) {
                    RollingAuditMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAuditMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = threshold

    fun snapshot(): RollingAuditMapping {
        return RollingAuditMapping(
            code = code,
            name = name,
            archived = archived,
            tags = tags,
            notes = notes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling audit mapping in status created. */
fun newRollingAuditMapping(code: String, name: String): RollingAuditMapping {
    return RollingAuditMapping(
        code = code,
        name = name,
        archived = false,
        tags = mutableListOf<String>(),
        notes = "",
        threshold = 0.0,
        status = RollingAuditMappingStatus.CREATED,
    )
}
