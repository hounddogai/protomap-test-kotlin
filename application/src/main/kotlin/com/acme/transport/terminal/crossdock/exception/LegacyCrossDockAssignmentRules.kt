package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize

/** A rule a legacy cross dock assignment must pass. Check returns its problems, or none. */
interface LegacyCrossDockAssignmentRule {
    fun check(item: LegacyCrossDockAssignment): MutableList<String>
}

data class LegacyCrossDockAssignmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockAssignmentCodeLengthRule(var maxLength: Int) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockAssignmentRequiredShiftCodeRule(var message: String) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockAssignmentCodePrefixRule(var prefix: String) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockAssignmentDescriptionLengthRule(var maxLength: Int) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockAssignmentRequiredDescriptionRule(
    var message: String,
) : LegacyCrossDockAssignmentRule {
    override fun check(item: LegacyCrossDockAssignment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy cross dock assignment. */
data class LegacyCrossDockAssignmentValidator(var rules: MutableList<LegacyCrossDockAssignmentRule>) {
    fun validate(item: LegacyCrossDockAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCrossDockAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCrossDockAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCrossDockAssignmentValidator(): LegacyCrossDockAssignmentValidator {
    return LegacyCrossDockAssignmentValidator(
        mutableListOf<LegacyCrossDockAssignmentRule>(
            LegacyCrossDockAssignmentAllowedLabelRule(mutableListOf<String>("tiered")),
            LegacyCrossDockAssignmentCodeLengthRule(1000),
            LegacyCrossDockAssignmentRequiredShiftCodeRule("legacy cross dock assignment needs a value"),
            LegacyCrossDockAssignmentCodePrefixRule("Q"),
            LegacyCrossDockAssignmentDescriptionLengthRule(86400),
            LegacyCrossDockAssignmentRequiredDescriptionRule("legacy cross dock assignment needs a value"),
        ),
    )
}
