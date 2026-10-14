package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery template is in its life cycle. */
enum class ProofOfDeliveryTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A proof of delivery template as the proof of delivery team keeps it. */
data class ProofOfDeliveryTemplate(
    var code: String,
    var label: String,
    var timezone: String,
    var revision: Int,
    var priority: Int,
    var description: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var windowDays: Int,
    var status: ProofOfDeliveryTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryTemplateStatus): Boolean {
        when (status) {
            ProofOfDeliveryTemplateStatus.CREATED -> {
                when (next) {
                    ProofOfDeliveryTemplateStatus.VALIDATED -> return true
                }
                return false
            }
            ProofOfDeliveryTemplateStatus.VALIDATED -> return next == ProofOfDeliveryTemplateStatus.PUBLISHED
            ProofOfDeliveryTemplateStatus.PUBLISHED -> {
                when (next) {
                    ProofOfDeliveryTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryTemplateStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"proof of delivery template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(priority.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryTemplate {
        return ProofOfDeliveryTemplate(
            code = code,
            label = label,
            timezone = timezone,
            revision = revision,
            priority = priority,
            description = description,
            attributes = attributes,
            tags = tags,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery template in status created. */
fun newProofOfDeliveryTemplate(code: String, label: String): ProofOfDeliveryTemplate {
    return ProofOfDeliveryTemplate(
        code = code,
        label = label,
        timezone = "",
        revision = 0,
        priority = 0,
        description = "",
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        windowDays = 0,
        status = ProofOfDeliveryTemplateStatus.CREATED,
    )
}
