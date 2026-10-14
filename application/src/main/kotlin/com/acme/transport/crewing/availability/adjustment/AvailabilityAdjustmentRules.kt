package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a availability adjustment must pass. Check returns its problems, or none. */
interface AvailabilityAdjustmentRule {
    fun check(item: AvailabilityAdjustment): MutableList<String>
}

data class AvailabilityAdjustmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : AvailabilityAdjustmentRule {
    override fun check(item: AvailabilityAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAdjustmentRequiredCodeRule(var message: String) : AvailabilityAdjustmentRule {
    override fun check(item: AvailabilityAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAdjustmentTitleLengthRule(var maxLength: Int) : AvailabilityAdjustmentRule {
    override fun check(item: AvailabilityAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability adjustment. */
data class AvailabilityAdjustmentValidator(var rules: MutableList<AvailabilityAdjustmentRule>) {
    fun validate(item: AvailabilityAdjustment): MutableList<String> {
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

    fun requireValid(item: AvailabilityAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityAdjustmentValidator(): AvailabilityAdjustmentValidator {
    return AvailabilityAdjustmentValidator(
        mutableListOf<AvailabilityAdjustmentRule>(
            AvailabilityAdjustmentAllowedTitleRule(mutableListOf<String>("pending", "regional", "weighted")),
            AvailabilityAdjustmentRequiredCodeRule("availability adjustment needs an entry"),
            AvailabilityAdjustmentTitleLengthRule(255),
        ),
    )
}
