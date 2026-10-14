package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.normalize

// The weighted emission factor template records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a weighted emission factor template is in its life cycle. */
enum class WeightedEmissionFactorTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted emission factor template and its current status. */
data class WeightedEmissionFactorTemplate(
    var code: String,
    var name: String,
    var priority: Int,
    var contactEmail: String,
    var category: String,
    var visible: Boolean,
    var status: WeightedEmissionFactorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEmissionFactorTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEmissionFactorTemplateStatus): Boolean {
        when (status) {
            WeightedEmissionFactorTemplateStatus.NEW -> {
                when (next) {
                    WeightedEmissionFactorTemplateStatus.RUNNING -> return true
                }
                return false
            }
            WeightedEmissionFactorTemplateStatus.QUEUED -> return next == WeightedEmissionFactorTemplateStatus.DONE
            WeightedEmissionFactorTemplateStatus.RUNNING -> return next == WeightedEmissionFactorTemplateStatus.FAILED
            WeightedEmissionFactorTemplateStatus.DONE -> return next == WeightedEmissionFactorTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedEmissionFactorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted emission factor template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted emission factor template"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): WeightedEmissionFactorTemplate {
        return WeightedEmissionFactorTemplate(
            code = code,
            name = name,
            priority = priority,
            contactEmail = contactEmail,
            category = category,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted emission factor template in status new. */
fun newWeightedEmissionFactorTemplate(code: String, name: String): WeightedEmissionFactorTemplate {
    return WeightedEmissionFactorTemplate(
        code = code,
        name = name,
        priority = 0,
        contactEmail = "",
        category = "",
        visible = false,
        status = WeightedEmissionFactorTemplateStatus.NEW,
    )
}
