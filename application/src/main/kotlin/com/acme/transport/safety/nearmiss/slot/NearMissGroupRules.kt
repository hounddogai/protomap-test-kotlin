package com.acme.transport.safety.nearmiss.slot

/** A rule a near miss group must pass. Check returns its problems, or none. */
interface NearMissGroupRule {
    fun check(item: NearMissGroup): MutableList<String>
}

data class NearMissGroupDescriptionLengthRule(var maxLength: Int) : NearMissGroupRule {
    override fun check(item: NearMissGroup): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissGroupEtaMinutesRangeRule(var min: Int, var max: Int) : NearMissGroupRule {
    override fun check(item: NearMissGroup): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class NearMissGroupCodePrefixRule(var prefix: String) : NearMissGroupRule {
    override fun check(item: NearMissGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss group. */
data class NearMissGroupValidator(var rules: MutableList<NearMissGroupRule>) {
    fun validate(item: NearMissGroup): MutableList<String> {
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

    fun requireValid(item: NearMissGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissGroupValidator(): NearMissGroupValidator {
    return NearMissGroupValidator(
        mutableListOf<NearMissGroupRule>(
            NearMissGroupDescriptionLengthRule(32),
            NearMissGroupEtaMinutesRangeRule(32, 1000),
            NearMissGroupCodePrefixRule("Q"),
        ),
    )
}
