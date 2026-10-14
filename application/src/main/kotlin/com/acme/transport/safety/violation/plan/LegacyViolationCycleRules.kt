package com.acme.transport.safety.violation.plan

/** A rule a legacy violation cycle must pass. Check returns its problems, or none. */
interface LegacyViolationCycleRule {
    fun check(item: LegacyViolationCycle): MutableList<String>
}

data class LegacyViolationCycleCodePrefixRule(var prefix: String) : LegacyViolationCycleRule {
    override fun check(item: LegacyViolationCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationCycleRouteCodeLengthRule(var maxLength: Int) : LegacyViolationCycleRule {
    override fun check(item: LegacyViolationCycle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationCycleCodeLengthRule(var maxLength: Int) : LegacyViolationCycleRule {
    override fun check(item: LegacyViolationCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy violation cycle. */
data class LegacyViolationCycleValidator(var rules: MutableList<LegacyViolationCycleRule>) {
    fun validate(item: LegacyViolationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyViolationCycleValidator(): LegacyViolationCycleValidator {
    return LegacyViolationCycleValidator(
        mutableListOf<LegacyViolationCycleRule>(
            LegacyViolationCycleCodePrefixRule("Q"),
            LegacyViolationCycleRouteCodeLengthRule(86400),
            LegacyViolationCycleCodeLengthRule(64),
        ),
    )
}
