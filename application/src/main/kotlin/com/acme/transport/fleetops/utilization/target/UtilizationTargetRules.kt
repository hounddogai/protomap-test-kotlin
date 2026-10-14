package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize

/** A rule a utilization target must pass. Check returns its problems, or none. */
interface UtilizationTargetRule {
    fun check(item: UtilizationTarget): MutableList<String>
}

data class UtilizationTargetRequiredCodeRule(var message: String) : UtilizationTargetRule {
    override fun check(item: UtilizationTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTargetFloorValueRangeRule(var min: Double, var max: Double) : UtilizationTargetRule {
    override fun check(item: UtilizationTarget): MutableList<String> {
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

data class UtilizationTargetRequiredSourceRule(var message: String) : UtilizationTargetRule {
    override fun check(item: UtilizationTarget): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTargetAllowedCodeRule(var allowed: MutableList<String>) : UtilizationTargetRule {
    override fun check(item: UtilizationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTargetCodeLengthRule(var maxLength: Int) : UtilizationTargetRule {
    override fun check(item: UtilizationTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization target. */
data class UtilizationTargetValidator(var rules: MutableList<UtilizationTargetRule>) {
    fun validate(item: UtilizationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationTargetValidator(): UtilizationTargetValidator {
    return UtilizationTargetValidator(
        mutableListOf<UtilizationTargetRule>(
            UtilizationTargetRequiredCodeRule("utilization target needs a value"),
            UtilizationTargetFloorValueRangeRule(1.0, 4553.5),
            UtilizationTargetRequiredSourceRule("utilization target needs an entry"),
            UtilizationTargetAllowedCodeRule(mutableListOf<String>()),
            UtilizationTargetCodeLengthRule(1000),
        ),
    )
}
