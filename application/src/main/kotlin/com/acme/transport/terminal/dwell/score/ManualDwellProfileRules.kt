package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize

/** A rule a manual dwell profile must pass. Check returns its problems, or none. */
interface ManualDwellProfileRule {
    fun check(item: ManualDwellProfile): MutableList<String>
}

data class ManualDwellProfileFloorValueRangeRule(var min: Double, var max: Double) : ManualDwellProfileRule {
    override fun check(item: ManualDwellProfile): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ManualDwellProfileCodePrefixRule(var prefix: String) : ManualDwellProfileRule {
    override fun check(item: ManualDwellProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellProfileAllowedTitleRule(var allowed: MutableList<String>) : ManualDwellProfileRule {
    override fun check(item: ManualDwellProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellProfileRequiredTitleRule(var message: String) : ManualDwellProfileRule {
    override fun check(item: ManualDwellProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellProfileTitleLengthRule(var maxLength: Int) : ManualDwellProfileRule {
    override fun check(item: ManualDwellProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dwell profile. */
data class ManualDwellProfileValidator(var rules: MutableList<ManualDwellProfileRule>) {
    fun validate(item: ManualDwellProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDwellProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellProfileValidator(): ManualDwellProfileValidator {
    return ManualDwellProfileValidator(
        mutableListOf<ManualDwellProfileRule>(
            ManualDwellProfileFloorValueRangeRule(9.7, 1057.9),
            ManualDwellProfileCodePrefixRule("REF"),
            ManualDwellProfileAllowedTitleRule(mutableListOf<String>("legacy", "strict", "rolling")),
            ManualDwellProfileRequiredTitleRule("manual dwell profile needs an entry"),
            ManualDwellProfileTitleLengthRule(1000),
        ),
    )
}
