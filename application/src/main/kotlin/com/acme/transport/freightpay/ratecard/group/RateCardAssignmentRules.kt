package com.acme.transport.freightpay.ratecard.group

/** A rule a rate card assignment must pass. Check returns its problems, or none. */
interface RateCardAssignmentRule {
    fun check(item: RateCardAssignment): MutableList<String>
}

data class RateCardAssignmentTitleLengthRule(var maxLength: Int) : RateCardAssignmentRule {
    override fun check(item: RateCardAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAssignmentVersionRangeRule(var min: Int, var max: Int) : RateCardAssignmentRule {
    override fun check(item: RateCardAssignment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAssignmentRequiredDriverIdRule(var message: String) : RateCardAssignmentRule {
    override fun check(item: RateCardAssignment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAssignmentCodePrefixRule(var prefix: String) : RateCardAssignmentRule {
    override fun check(item: RateCardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card assignment. */
data class RateCardAssignmentValidator(var rules: MutableList<RateCardAssignmentRule>) {
    fun validate(item: RateCardAssignment): MutableList<String> {
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

    fun requireValid(item: RateCardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardAssignmentValidator(): RateCardAssignmentValidator {
    return RateCardAssignmentValidator(
        mutableListOf<RateCardAssignmentRule>(
            RateCardAssignmentTitleLengthRule(64),
            RateCardAssignmentVersionRangeRule(80, 120),
            RateCardAssignmentRequiredDriverIdRule("rate card assignment needs an entry"),
            RateCardAssignmentCodePrefixRule("Q"),
        ),
    )
}
