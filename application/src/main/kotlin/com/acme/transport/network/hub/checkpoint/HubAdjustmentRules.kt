package com.acme.transport.network.hub.checkpoint

/** A rule a hub adjustment must pass. Check returns its problems, or none. */
interface HubAdjustmentRule {
    fun check(item: HubAdjustment): MutableList<String>
}

data class HubAdjustmentNameLengthRule(var maxLength: Int) : HubAdjustmentRule {
    override fun check(item: HubAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubAdjustmentAttemptsRangeRule(var min: Int, var max: Int) : HubAdjustmentRule {
    override fun check(item: HubAdjustment): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class HubAdjustmentRequiredNameRule(var message: String) : HubAdjustmentRule {
    override fun check(item: HubAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub adjustment. */
data class HubAdjustmentValidator(var rules: MutableList<HubAdjustmentRule>) {
    fun validate(item: HubAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultHubAdjustmentValidator(): HubAdjustmentValidator {
    return HubAdjustmentValidator(
        mutableListOf<HubAdjustmentRule>(
            HubAdjustmentNameLengthRule(32),
            HubAdjustmentAttemptsRangeRule(16, 120),
            HubAdjustmentRequiredNameRule("hub adjustment needs this field"),
        ),
    )
}
