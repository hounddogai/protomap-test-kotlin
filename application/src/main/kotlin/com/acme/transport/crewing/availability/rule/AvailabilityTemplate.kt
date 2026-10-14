package com.acme.transport.crewing.availability.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability template is in its life cycle. */
enum class AvailabilityTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A availability template. Its code is unique within a tenant. */
data class AvailabilityTemplate(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var visible: Boolean,
    var routeCode: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var revision: Int,
    var startsAt: Int,
    var maximum: Double,
    var status: AvailabilityTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityTemplateStatus): Boolean {
        when (status) {
            AvailabilityTemplateStatus.OPEN -> {
                when (next) {
                    AvailabilityTemplateStatus.REOPENED -> return true
                }
                return false
            }
            AvailabilityTemplateStatus.IN_REVIEW -> {
                when (next) {
                    AvailabilityTemplateStatus.RESOLVED -> return true
                }
                return false
            }
            AvailabilityTemplateStatus.RESOLVED -> {
                when (next) {
                    AvailabilityTemplateStatus.REOPENED, AvailabilityTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityTemplateStatus.REOPENED -> {
                when (next) {
                    AvailabilityTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityTemplateStatus) {
        if (!canMoveTo(next)) {
            error("availability template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"availability template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((revision.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityTemplate {
        return AvailabilityTemplate(
            code = code,
            label = label,
            pinned = pinned,
            visible = visible,
            routeCode = routeCode,
            region = region,
            attributes = attributes,
            reviewer = reviewer,
            revision = revision,
            startsAt = startsAt,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a availability template in status open. */
fun newAvailabilityTemplate(code: String, label: String): AvailabilityTemplate {
    return AvailabilityTemplate(
        code = code,
        label = label,
        pinned = false,
        visible = false,
        routeCode = "",
        region = "",
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        revision = 0,
        startsAt = 0,
        maximum = 0.0,
        status = AvailabilityTemplateStatus.OPEN,
    )
}
