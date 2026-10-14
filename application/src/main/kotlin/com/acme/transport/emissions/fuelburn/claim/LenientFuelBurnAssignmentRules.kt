package com.acme.transport.emissions.fuelburn.claim

/** A rule a lenient fuel burn assignment must pass. Check returns its problems, or none. */
interface LenientFuelBurnAssignmentRule {
    fun check(item: LenientFuelBurnAssignment): MutableList<String>
}

data class LenientFuelBurnAssignmentRequiredTitleRule(var message: String) : LenientFuelBurnAssignmentRule {
    override fun check(item: LenientFuelBurnAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnAssignmentLimitRangeRule(var min: Int, var max: Int) : LenientFuelBurnAssignmentRule {
    override fun check(item: LenientFuelBurnAssignment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LenientFuelBurnAssignmentTrailerIdLengthRule(var maxLength: Int) : LenientFuelBurnAssignmentRule {
    override fun check(item: LenientFuelBurnAssignment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnAssignmentCodePrefixRule(var prefix: String) : LenientFuelBurnAssignmentRule {
    override fun check(item: LenientFuelBurnAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnAssignmentRequiredCodeRule(var message: String) : LenientFuelBurnAssignmentRule {
    override fun check(item: LenientFuelBurnAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel burn assignment. */
data class LenientFuelBurnAssignmentValidator(var rules: MutableList<LenientFuelBurnAssignmentRule>) {
    fun validate(item: LenientFuelBurnAssignment): MutableList<String> {
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

    fun requireValid(item: LenientFuelBurnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelBurnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelBurnAssignmentValidator(): LenientFuelBurnAssignmentValidator {
    return LenientFuelBurnAssignmentValidator(
        mutableListOf<LenientFuelBurnAssignmentRule>(
            LenientFuelBurnAssignmentRequiredTitleRule("lenient fuel burn assignment needs this field"),
            LenientFuelBurnAssignmentLimitRangeRule(80, 86400),
            LenientFuelBurnAssignmentTrailerIdLengthRule(32),
            LenientFuelBurnAssignmentCodePrefixRule("REF"),
            LenientFuelBurnAssignmentRequiredCodeRule("lenient fuel burn assignment needs a value"),
        ),
    )
}
