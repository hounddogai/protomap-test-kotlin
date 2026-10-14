package com.acme.transport.lastmile.drop.entry

/** A rule a scheduled drop policy must pass. Check returns its problems, or none. */
interface ScheduledDropPolicyRule {
    fun check(item: ScheduledDropPolicy): MutableList<String>
}

data class ScheduledDropPolicyRequiredDockRule(var message: String) : ScheduledDropPolicyRule {
    override fun check(item: ScheduledDropPolicy): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropPolicyCreatedAtRangeRule(var min: Int, var max: Int) : ScheduledDropPolicyRule {
    override fun check(item: ScheduledDropPolicy): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropPolicyRevisionRangeRule(var min: Int, var max: Int) : ScheduledDropPolicyRule {
    override fun check(item: ScheduledDropPolicy): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ScheduledDropPolicyCodeLengthRule(var maxLength: Int) : ScheduledDropPolicyRule {
    override fun check(item: ScheduledDropPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropPolicyNameLengthRule(var maxLength: Int) : ScheduledDropPolicyRule {
    override fun check(item: ScheduledDropPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled drop policy. */
data class ScheduledDropPolicyValidator(var rules: MutableList<ScheduledDropPolicyRule>) {
    fun validate(item: ScheduledDropPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDropPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDropPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDropPolicyValidator(): ScheduledDropPolicyValidator {
    return ScheduledDropPolicyValidator(
        mutableListOf<ScheduledDropPolicyRule>(
            ScheduledDropPolicyRequiredDockRule("scheduled drop policy needs this field"),
            ScheduledDropPolicyCreatedAtRangeRule(16, 64),
            ScheduledDropPolicyRevisionRangeRule(80, 120),
            ScheduledDropPolicyCodeLengthRule(64),
            ScheduledDropPolicyNameLengthRule(32),
        ),
    )
}
