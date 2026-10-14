package com.acme.transport.safety.nearmiss.overrideitem

/** A rule a strict near miss override must pass. Check returns its problems, or none. */
interface StrictNearMissOverrideRule {
    fun check(item: StrictNearMissOverride): MutableList<String>
}

data class StrictNearMissOverrideCapacityRangeRule(var min: Int, var max: Int) : StrictNearMissOverrideRule {
    override fun check(item: StrictNearMissOverride): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class StrictNearMissOverrideRequiredContactEmailRule(var message: String) : StrictNearMissOverrideRule {
    override fun check(item: StrictNearMissOverride): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissOverrideRequiredNameRule(var message: String) : StrictNearMissOverrideRule {
    override fun check(item: StrictNearMissOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissOverridePriorityRangeRule(var min: Int, var max: Int) : StrictNearMissOverrideRule {
    override fun check(item: StrictNearMissOverride): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss override. */
data class StrictNearMissOverrideValidator(var rules: MutableList<StrictNearMissOverrideRule>) {
    fun validate(item: StrictNearMissOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: StrictNearMissOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissOverrideValidator(): StrictNearMissOverrideValidator {
    return StrictNearMissOverrideValidator(
        mutableListOf<StrictNearMissOverrideRule>(
            StrictNearMissOverrideCapacityRangeRule(120, 255),
            StrictNearMissOverrideRequiredContactEmailRule("strict near miss override needs a value"),
            StrictNearMissOverrideRequiredNameRule("strict near miss override needs a value"),
            StrictNearMissOverridePriorityRangeRule(1000, 86400),
        ),
    )
}
