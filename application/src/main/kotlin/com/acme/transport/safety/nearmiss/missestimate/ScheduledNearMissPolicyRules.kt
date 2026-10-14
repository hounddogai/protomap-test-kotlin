package com.acme.transport.safety.nearmiss.missestimate

/** A rule a scheduled near miss policy must pass. Check returns its problems, or none. */
interface ScheduledNearMissPolicyRule {
    fun check(item: ScheduledNearMissPolicy): MutableList<String>
}

data class ScheduledNearMissPolicyRequiredCodeRule(var message: String) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPolicyVisibleGuardRule(var limit: Double) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPolicyCodePrefixRule(var prefix: String) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissPolicyRateRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ScheduledNearMissPolicyPriorityRangeRule(var min: Int, var max: Int) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ScheduledNearMissPolicyCodeLengthRule(var maxLength: Int) : ScheduledNearMissPolicyRule {
    override fun check(item: ScheduledNearMissPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss policy. */
data class ScheduledNearMissPolicyValidator(var rules: MutableList<ScheduledNearMissPolicyRule>) {
    fun validate(item: ScheduledNearMissPolicy): MutableList<String> {
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

    fun requireValid(item: ScheduledNearMissPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissPolicyValidator(): ScheduledNearMissPolicyValidator {
    return ScheduledNearMissPolicyValidator(
        mutableListOf<ScheduledNearMissPolicyRule>(
            ScheduledNearMissPolicyRequiredCodeRule("scheduled near miss policy needs a value"),
            ScheduledNearMissPolicyVisibleGuardRule(9.4),
            ScheduledNearMissPolicyCodePrefixRule("Q"),
            ScheduledNearMissPolicyRateRangeRule(5.0, 1213.3),
            ScheduledNearMissPolicyPriorityRangeRule(3600, 86400),
            ScheduledNearMissPolicyCodeLengthRule(80),
        ),
    )
}
