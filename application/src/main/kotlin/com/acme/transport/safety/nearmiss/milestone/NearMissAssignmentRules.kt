package com.acme.transport.safety.nearmiss.milestone

/** A rule a near miss assignment must pass. Check returns its problems, or none. */
interface NearMissAssignmentRule {
    fun check(item: NearMissAssignment): MutableList<String>
}

data class NearMissAssignmentDepotLengthRule(var maxLength: Int) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAssignmentDurationMinutesRangeRule(var min: Int, var max: Int) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class NearMissAssignmentCodePrefixRule(var prefix: String) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAssignmentRequiredCodeRule(var message: String) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissAssignmentRequiredDepotRule(var message: String) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissAssignmentAmountRangeRule(var min: Double, var max: Double) : NearMissAssignmentRule {
    override fun check(item: NearMissAssignment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss assignment. */
data class NearMissAssignmentValidator(var rules: MutableList<NearMissAssignmentRule>) {
    fun validate(item: NearMissAssignment): MutableList<String> {
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

    fun requireValid(item: NearMissAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissAssignmentValidator(): NearMissAssignmentValidator {
    return NearMissAssignmentValidator(
        mutableListOf<NearMissAssignmentRule>(
            NearMissAssignmentDepotLengthRule(1000),
            NearMissAssignmentDurationMinutesRangeRule(64, 255),
            NearMissAssignmentCodePrefixRule("ACM"),
            NearMissAssignmentRequiredCodeRule("near miss assignment needs an entry"),
            NearMissAssignmentRequiredDepotRule("near miss assignment needs this field"),
            NearMissAssignmentAmountRangeRule(5.2, 4864.1),
        ),
    )
}
