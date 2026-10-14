package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize

/** A rule a scheduled trailer yard adjustment must pass. Check returns its problems, or none. */
interface ScheduledTrailerYardAdjustmentRule {
    fun check(item: ScheduledTrailerYardAdjustment): MutableList<String>
}

data class ScheduledTrailerYardAdjustmentRequiredOwnerIdRule(
    var message: String,
) : ScheduledTrailerYardAdjustmentRule {
    override fun check(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardAdjustmentRequiredCodeRule(
    var message: String,
) : ScheduledTrailerYardAdjustmentRule {
    override fun check(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardAdjustmentRequiredDockRule(
    var message: String,
) : ScheduledTrailerYardAdjustmentRule {
    override fun check(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardAdjustmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTrailerYardAdjustmentRule {
    override fun check(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ScheduledTrailerYardAdjustmentAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledTrailerYardAdjustmentRule {
    override fun check(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer yard adjustment. */
data class ScheduledTrailerYardAdjustmentValidator(var rules: MutableList<ScheduledTrailerYardAdjustmentRule>) {
    fun validate(item: ScheduledTrailerYardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTrailerYardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerYardAdjustment>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultScheduledTrailerYardAdjustmentValidator(): ScheduledTrailerYardAdjustmentValidator {
    return ScheduledTrailerYardAdjustmentValidator(
        mutableListOf<ScheduledTrailerYardAdjustmentRule>(
            ScheduledTrailerYardAdjustmentRequiredOwnerIdRule("scheduled trailer yard adjustment needs a value"),
            ScheduledTrailerYardAdjustmentRequiredCodeRule("scheduled trailer yard adjustment needs an entry"),
            ScheduledTrailerYardAdjustmentRequiredDockRule("scheduled trailer yard adjustment needs this field"),
            ScheduledTrailerYardAdjustmentDiscountRangeRule(7.5, 478.2),
            ScheduledTrailerYardAdjustmentAllowedNameRule(mutableListOf<String>("default")),
        ),
    )
}
