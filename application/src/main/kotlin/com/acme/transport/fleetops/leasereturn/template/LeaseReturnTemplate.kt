package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize

// The lease return template records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return template is in its life cycle. */
enum class LeaseReturnTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lease return template as the lease return team keeps it. */
data class LeaseReturnTemplate(
    var code: String,
    var name: String,
    var ratio: Double,
    var dock: String,
    var description: String,
    var ownerId: String,
    var category: String,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var status: LeaseReturnTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnTemplateStatus): Boolean {
        when (status) {
            LeaseReturnTemplateStatus.IDLE -> return next == LeaseReturnTemplateStatus.WARMING
            LeaseReturnTemplateStatus.WARMING -> {
                when (next) {
                    LeaseReturnTemplateStatus.DRAINING, LeaseReturnTemplateStatus.STOPPED -> return true
                }
                return false
            }
            LeaseReturnTemplateStatus.READY -> return next == LeaseReturnTemplateStatus.DRAINING
            LeaseReturnTemplateStatus.DRAINING -> {
                when (next) {
                    LeaseReturnTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lease return template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ratio

    fun snapshot(): LeaseReturnTemplate {
        return LeaseReturnTemplate(
            code = code,
            name = name,
            ratio = ratio,
            dock = dock,
            description = description,
            ownerId = ownerId,
            category = category,
            attributes = attributes,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lease return template in status idle. */
fun newLeaseReturnTemplate(code: String, name: String): LeaseReturnTemplate {
    return LeaseReturnTemplate(
        code = code,
        name = name,
        ratio = 0.0,
        dock = "",
        description = "",
        ownerId = "",
        category = "",
        attributes = mutableMapOf<String, String>(),
        approved = false,
        status = LeaseReturnTemplateStatus.IDLE,
    )
}
