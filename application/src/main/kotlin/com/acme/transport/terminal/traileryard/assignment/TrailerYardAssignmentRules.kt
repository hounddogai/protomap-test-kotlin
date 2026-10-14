package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard assignment must pass. Check returns its problems, or none. */
interface TrailerYardAssignmentRule {
    fun check(item: TrailerYardAssignment): MutableList<String>
}

data class TrailerYardAssignmentAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardAssignmentRule {
    override fun check(item: TrailerYardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAssignmentVisibleGuardRule(var limit: Double) : TrailerYardAssignmentRule {
    override fun check(item: TrailerYardAssignment): MutableList<String> {
        if (item.visible && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAssignmentCodePrefixRule(var prefix: String) : TrailerYardAssignmentRule {
    override fun check(item: TrailerYardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAssignmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TrailerYardAssignmentRule {
    override fun check(item: TrailerYardAssignment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class TrailerYardAssignmentOwnerIdLengthRule(var maxLength: Int) : TrailerYardAssignmentRule {
    override fun check(item: TrailerYardAssignment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard assignment. */
data class TrailerYardAssignmentValidator(var rules: MutableList<TrailerYardAssignmentRule>) {
    fun validate(item: TrailerYardAssignment): MutableList<String> {
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

    fun requireValid(item: TrailerYardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardAssignmentValidator(): TrailerYardAssignmentValidator {
    return TrailerYardAssignmentValidator(
        mutableListOf<TrailerYardAssignmentRule>(
            TrailerYardAssignmentAllowedCodeRule(mutableListOf<String>("adaptive")),
            TrailerYardAssignmentVisibleGuardRule(8.9),
            TrailerYardAssignmentCodePrefixRule("OPS"),
            TrailerYardAssignmentThresholdRangeRule(6.3, 3884.1),
            TrailerYardAssignmentOwnerIdLengthRule(16),
        ),
    )
}
