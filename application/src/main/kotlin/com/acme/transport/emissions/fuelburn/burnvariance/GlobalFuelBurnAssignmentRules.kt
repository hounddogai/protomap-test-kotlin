package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize

/** A rule a global fuel burn assignment must pass. Check returns its problems, or none. */
interface GlobalFuelBurnAssignmentRule {
    fun check(item: GlobalFuelBurnAssignment): MutableList<String>
}

data class GlobalFuelBurnAssignmentAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalFuelBurnAssignmentRule {
    override fun check(item: GlobalFuelBurnAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnAssignmentCodeLengthRule(var maxLength: Int) : GlobalFuelBurnAssignmentRule {
    override fun check(item: GlobalFuelBurnAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnAssignmentCodePrefixRule(var prefix: String) : GlobalFuelBurnAssignmentRule {
    override fun check(item: GlobalFuelBurnAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel burn assignment. */
data class GlobalFuelBurnAssignmentValidator(var rules: MutableList<GlobalFuelBurnAssignmentRule>) {
    fun validate(item: GlobalFuelBurnAssignment): MutableList<String> {
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

    fun requireValid(item: GlobalFuelBurnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelBurnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelBurnAssignmentValidator(): GlobalFuelBurnAssignmentValidator {
    return GlobalFuelBurnAssignmentValidator(
        mutableListOf<GlobalFuelBurnAssignmentRule>(
            GlobalFuelBurnAssignmentAllowedNameRule(mutableListOf<String>("pending", "batched")),
            GlobalFuelBurnAssignmentCodeLengthRule(16),
            GlobalFuelBurnAssignmentCodePrefixRule("X"),
        ),
    )
}
