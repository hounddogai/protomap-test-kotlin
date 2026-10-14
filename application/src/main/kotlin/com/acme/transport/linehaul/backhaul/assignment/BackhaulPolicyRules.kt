package com.acme.transport.linehaul.backhaul.assignment

/** A rule a backhaul policy must pass. Check returns its problems, or none. */
interface BackhaulPolicyRule {
    fun check(item: BackhaulPolicy): MutableList<String>
}

data class BackhaulPolicyUnitLengthRule(var maxLength: Int) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPolicyStartsAtRangeRule(var min: Int, var max: Int) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class BackhaulPolicyPriorityRangeRule(var min: Int, var max: Int) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPolicyRequiredTitleRule(var message: String) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPolicyCodeLengthRule(var maxLength: Int) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulPolicyTitleLengthRule(var maxLength: Int) : BackhaulPolicyRule {
    override fun check(item: BackhaulPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul policy. */
data class BackhaulPolicyValidator(var rules: MutableList<BackhaulPolicyRule>) {
    fun validate(item: BackhaulPolicy): MutableList<String> {
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

    fun requireValid(item: BackhaulPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulPolicyValidator(): BackhaulPolicyValidator {
    return BackhaulPolicyValidator(
        mutableListOf<BackhaulPolicyRule>(
            BackhaulPolicyUnitLengthRule(32),
            BackhaulPolicyStartsAtRangeRule(32, 1000),
            BackhaulPolicyPriorityRangeRule(64, 1000),
            BackhaulPolicyRequiredTitleRule("backhaul policy needs an entry"),
            BackhaulPolicyCodeLengthRule(80),
            BackhaulPolicyTitleLengthRule(120),
        ),
    )
}
