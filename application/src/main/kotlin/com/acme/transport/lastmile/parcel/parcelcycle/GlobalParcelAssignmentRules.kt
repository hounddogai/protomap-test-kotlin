package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize

/** A rule a global parcel assignment must pass. Check returns its problems, or none. */
interface GlobalParcelAssignmentRule {
    fun check(item: GlobalParcelAssignment): MutableList<String>
}

data class GlobalParcelAssignmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalParcelAssignmentRule {
    override fun check(item: GlobalParcelAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelAssignmentRequiredCodeRule(var message: String) : GlobalParcelAssignmentRule {
    override fun check(item: GlobalParcelAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelAssignmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : GlobalParcelAssignmentRule {
    override fun check(item: GlobalParcelAssignment): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelAssignmentCodePrefixRule(var prefix: String) : GlobalParcelAssignmentRule {
    override fun check(item: GlobalParcelAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global parcel assignment. */
data class GlobalParcelAssignmentValidator(var rules: MutableList<GlobalParcelAssignmentRule>) {
    fun validate(item: GlobalParcelAssignment): MutableList<String> {
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

    fun requireValid(item: GlobalParcelAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalParcelAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalParcelAssignmentValidator(): GlobalParcelAssignmentValidator {
    return GlobalParcelAssignmentValidator(
        mutableListOf<GlobalParcelAssignmentRule>(
            GlobalParcelAssignmentAllowedCodeRule(mutableListOf<String>("weighted", "tiered")),
            GlobalParcelAssignmentRequiredCodeRule("global parcel assignment needs an entry"),
            GlobalParcelAssignmentCeilingRangeRule(2.7, 4596.2),
            GlobalParcelAssignmentCodePrefixRule("OPS"),
        ),
    )
}
