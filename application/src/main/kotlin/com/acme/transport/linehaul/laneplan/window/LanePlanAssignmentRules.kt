package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize

/** A rule a lane plan assignment must pass. Check returns its problems, or none. */
interface LanePlanAssignmentRule {
    fun check(item: LanePlanAssignment): MutableList<String>
}

data class LanePlanAssignmentWindowDaysRangeRule(var min: Int, var max: Int) : LanePlanAssignmentRule {
    override fun check(item: LanePlanAssignment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LanePlanAssignmentRequiredNameRule(var message: String) : LanePlanAssignmentRule {
    override fun check(item: LanePlanAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAssignmentAllowedSourceRule(var allowed: MutableList<String>) : LanePlanAssignmentRule {
    override fun check(item: LanePlanAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAssignmentCodePrefixRule(var prefix: String) : LanePlanAssignmentRule {
    override fun check(item: LanePlanAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanAssignmentRequiredChecksumRule(var message: String) : LanePlanAssignmentRule {
    override fun check(item: LanePlanAssignment): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan assignment. */
data class LanePlanAssignmentValidator(var rules: MutableList<LanePlanAssignmentRule>) {
    fun validate(item: LanePlanAssignment): MutableList<String> {
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

    fun requireValid(item: LanePlanAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanAssignmentValidator(): LanePlanAssignmentValidator {
    return LanePlanAssignmentValidator(
        mutableListOf<LanePlanAssignmentRule>(
            LanePlanAssignmentWindowDaysRangeRule(64, 120),
            LanePlanAssignmentRequiredNameRule("lane plan assignment needs an entry"),
            LanePlanAssignmentAllowedSourceRule(mutableListOf<String>("rolling", "legacy")),
            LanePlanAssignmentCodePrefixRule("Q"),
            LanePlanAssignmentRequiredChecksumRule("lane plan assignment needs an entry"),
        ),
    )
}
