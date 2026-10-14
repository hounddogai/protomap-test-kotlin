package com.acme.transport.crewing.availability.claim

/** A rule a lenient availability allocation must pass. Check returns its problems, or none. */
interface LenientAvailabilityAllocationRule {
    fun check(item: LenientAvailabilityAllocation): MutableList<String>
}

data class LenientAvailabilityAllocationTitleLengthRule(
    var maxLength: Int,
) : LenientAvailabilityAllocationRule {
    override fun check(item: LenientAvailabilityAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityAllocationRequiredTitleRule(
    var message: String,
) : LenientAvailabilityAllocationRule {
    override fun check(item: LenientAvailabilityAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityAllocationBudgetRangeRule(
    var min: Double,
    var max: Double,
) : LenientAvailabilityAllocationRule {
    override fun check(item: LenientAvailabilityAllocation): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityAllocationRequiredLabelRule(
    var message: String,
) : LenientAvailabilityAllocationRule {
    override fun check(item: LenientAvailabilityAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient availability allocation. */
data class LenientAvailabilityAllocationValidator(var rules: MutableList<LenientAvailabilityAllocationRule>) {
    fun validate(item: LenientAvailabilityAllocation): MutableList<String> {
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

    fun requireValid(item: LenientAvailabilityAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAvailabilityAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAvailabilityAllocationValidator(): LenientAvailabilityAllocationValidator {
    return LenientAvailabilityAllocationValidator(
        mutableListOf<LenientAvailabilityAllocationRule>(
            LenientAvailabilityAllocationTitleLengthRule(86400),
            LenientAvailabilityAllocationRequiredTitleRule("lenient availability allocation needs an entry"),
            LenientAvailabilityAllocationBudgetRangeRule(9.9, 1092.2),
            LenientAvailabilityAllocationRequiredLabelRule("lenient availability allocation needs an entry"),
        ),
    )
}
