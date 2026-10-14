package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt template records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt template is in its life cycle. */
enum class AttemptTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One attempt template, with what the planners need to act on it. */
data class AttemptTemplate(
    var code: String,
    var title: String,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var visible: Boolean,
    var updatedAt: Int,
    var score: Double,
    var minimum: Double,
    var status: AttemptTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptTemplateStatus): Boolean {
        when (status) {
            AttemptTemplateStatus.PLANNED -> return next == AttemptTemplateStatus.SCHEDULED || next == AttemptTemplateStatus.CANCELLED
            AttemptTemplateStatus.SCHEDULED -> {
                when (next) {
                    AttemptTemplateStatus.IN_PROGRESS, AttemptTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            AttemptTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    AttemptTemplateStatus.COMPLETED, AttemptTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            AttemptTemplateStatus.COMPLETED -> {
                when (next) {
                    AttemptTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptTemplateStatus) {
        if (!canMoveTo(next)) {
            error("attempt template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((updatedAt.toDouble() - rate) * 100) / 100

    fun snapshot(): AttemptTemplate {
        return AttemptTemplate(
            code = code,
            title = title,
            rate = rate,
            attributes = attributes,
            maximum = maximum,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            visible = visible,
            updatedAt = updatedAt,
            score = score,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a attempt template in status planned. */
fun newAttemptTemplate(code: String, title: String): AttemptTemplate {
    return AttemptTemplate(
        code = code,
        title = title,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        durationMinutes = 0,
        fuelLiters = 0.0,
        visible = false,
        updatedAt = 0,
        score = 0.0,
        minimum = 0.0,
        status = AttemptTemplateStatus.PLANNED,
    )
}
