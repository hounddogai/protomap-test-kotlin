package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.text.normalize

/** A rule a cross dock assignment must pass. Check returns its problems, or none. */
interface CrossDockAssignmentRule {
    fun check(item: CrossDockAssignment): MutableList<String>
}

data class CrossDockAssignmentCodeLengthRule(var maxLength: Int) : CrossDockAssignmentRule {
    override fun check(item: CrossDockAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAssignmentStopCountRangeRule(var min: Int, var max: Int) : CrossDockAssignmentRule {
    override fun check(item: CrossDockAssignment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAssignmentThresholdRangeRule(var min: Double, var max: Double) : CrossDockAssignmentRule {
    override fun check(item: CrossDockAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAssignmentAllowedLocaleRule(var allowed: MutableList<String>) : CrossDockAssignmentRule {
    override fun check(item: CrossDockAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAssignmentTitleLengthRule(var maxLength: Int) : CrossDockAssignmentRule {
    override fun check(item: CrossDockAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock assignment. */
data class CrossDockAssignmentValidator(var rules: MutableList<CrossDockAssignmentRule>) {
    fun validate(item: CrossDockAssignment): MutableList<String> {
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

    fun requireValid(item: CrossDockAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockAssignmentValidator(): CrossDockAssignmentValidator {
    return CrossDockAssignmentValidator(
        mutableListOf<CrossDockAssignmentRule>(
            CrossDockAssignmentCodeLengthRule(86400),
            CrossDockAssignmentStopCountRangeRule(16, 80),
            CrossDockAssignmentThresholdRangeRule(8.1, 981.6),
            CrossDockAssignmentAllowedLocaleRule(mutableListOf<String>("weighted")),
            CrossDockAssignmentTitleLengthRule(16),
        ),
    )
}
