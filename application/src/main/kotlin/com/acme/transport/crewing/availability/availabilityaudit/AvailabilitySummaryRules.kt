package com.acme.transport.crewing.availability.availabilityaudit

/** A rule a availability summary must pass. Check returns its problems, or none. */
interface AvailabilitySummaryRule {
    fun check(item: AvailabilitySummary): MutableList<String>
}

data class AvailabilitySummaryRequiredCodeRule(var message: String) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySummaryCodePrefixRule(var prefix: String) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySummaryShiftCodeLengthRule(var maxLength: Int) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySummaryReviewerLengthRule(var maxLength: Int) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySummaryEndsAtRangeRule(var min: Int, var max: Int) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class AvailabilitySummaryQuantityRangeRule(var min: Int, var max: Int) : AvailabilitySummaryRule {
    override fun check(item: AvailabilitySummary): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a availability summary. */
data class AvailabilitySummaryValidator(var rules: MutableList<AvailabilitySummaryRule>) {
    fun validate(item: AvailabilitySummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilitySummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilitySummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilitySummaryValidator(): AvailabilitySummaryValidator {
    return AvailabilitySummaryValidator(
        mutableListOf<AvailabilitySummaryRule>(
            AvailabilitySummaryRequiredCodeRule("availability summary needs an entry"),
            AvailabilitySummaryCodePrefixRule("ACM"),
            AvailabilitySummaryShiftCodeLengthRule(86400),
            AvailabilitySummaryReviewerLengthRule(80),
            AvailabilitySummaryEndsAtRangeRule(120, 86400),
            AvailabilitySummaryQuantityRangeRule(16, 255),
        ),
    )
}
