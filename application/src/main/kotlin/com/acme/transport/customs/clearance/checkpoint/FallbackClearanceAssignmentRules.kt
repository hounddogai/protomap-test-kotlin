package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a fallback clearance assignment must pass. Check returns its problems, or none. */
interface FallbackClearanceAssignmentRule {
    fun check(item: FallbackClearanceAssignment): MutableList<String>
}

data class FallbackClearanceAssignmentCodePrefixRule(var prefix: String) : FallbackClearanceAssignmentRule {
    override fun check(item: FallbackClearanceAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceAssignmentTimezoneLengthRule(var maxLength: Int) : FallbackClearanceAssignmentRule {
    override fun check(item: FallbackClearanceAssignment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceAssignmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : FallbackClearanceAssignmentRule {
    override fun check(item: FallbackClearanceAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceAssignmentRequiredTimezoneRule(
    var message: String,
) : FallbackClearanceAssignmentRule {
    override fun check(item: FallbackClearanceAssignment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceAssignmentAllowedNotesRule(
    var allowed: MutableList<String>,
) : FallbackClearanceAssignmentRule {
    override fun check(item: FallbackClearanceAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback clearance assignment. */
data class FallbackClearanceAssignmentValidator(var rules: MutableList<FallbackClearanceAssignmentRule>) {
    fun validate(item: FallbackClearanceAssignment): MutableList<String> {
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

    fun requireValid(item: FallbackClearanceAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackClearanceAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackClearanceAssignmentValidator(): FallbackClearanceAssignmentValidator {
    return FallbackClearanceAssignmentValidator(
        mutableListOf<FallbackClearanceAssignmentRule>(
            FallbackClearanceAssignmentCodePrefixRule("OPS"),
            FallbackClearanceAssignmentTimezoneLengthRule(32),
            FallbackClearanceAssignmentThresholdRangeRule(6.3, 3484.2),
            FallbackClearanceAssignmentRequiredTimezoneRule("fallback clearance assignment needs a value"),
            FallbackClearanceAssignmentAllowedNotesRule(mutableListOf<String>("lenient")),
        ),
    )
}
