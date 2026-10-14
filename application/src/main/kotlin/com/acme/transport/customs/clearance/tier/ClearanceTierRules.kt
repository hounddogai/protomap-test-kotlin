package com.acme.transport.customs.clearance.tier

/** A rule a clearance tier must pass. Check returns its problems, or none. */
interface ClearanceTierRule {
    fun check(item: ClearanceTier): MutableList<String>
}

data class ClearanceTierSequenceRangeRule(var min: Int, var max: Int) : ClearanceTierRule {
    override fun check(item: ClearanceTier): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ClearanceTierContactEmailLengthRule(var maxLength: Int) : ClearanceTierRule {
    override fun check(item: ClearanceTier): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceTierActiveGuardRule(var limit: Double) : ClearanceTierRule {
    override fun check(item: ClearanceTier): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance tier. */
data class ClearanceTierValidator(var rules: MutableList<ClearanceTierRule>) {
    fun validate(item: ClearanceTier): MutableList<String> {
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

    fun requireValid(item: ClearanceTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceTier>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceTierValidator(): ClearanceTierValidator {
    return ClearanceTierValidator(
        mutableListOf<ClearanceTierRule>(
            ClearanceTierSequenceRangeRule(64, 86400),
            ClearanceTierContactEmailLengthRule(86400),
            ClearanceTierActiveGuardRule(2.9),
        ),
    )
}
