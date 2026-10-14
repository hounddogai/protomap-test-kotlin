package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.text.normalize

/** A rule a pending service plan rollup must pass. Check returns its problems, or none. */
interface PendingServicePlanRollupRule {
    fun check(item: PendingServicePlanRollup): MutableList<String>
}

data class PendingServicePlanRollupCodePrefixRule(var prefix: String) : PendingServicePlanRollupRule {
    override fun check(item: PendingServicePlanRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanRollupLimitRangeRule(var min: Int, var max: Int) : PendingServicePlanRollupRule {
    override fun check(item: PendingServicePlanRollup): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanRollupAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingServicePlanRollupRule {
    override fun check(item: PendingServicePlanRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanRollupShiftCodeLengthRule(var maxLength: Int) : PendingServicePlanRollupRule {
    override fun check(item: PendingServicePlanRollup): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service plan rollup. */
data class PendingServicePlanRollupValidator(var rules: MutableList<PendingServicePlanRollupRule>) {
    fun validate(item: PendingServicePlanRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServicePlanRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServicePlanRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServicePlanRollupValidator(): PendingServicePlanRollupValidator {
    return PendingServicePlanRollupValidator(
        mutableListOf<PendingServicePlanRollupRule>(
            PendingServicePlanRollupCodePrefixRule("OPS"),
            PendingServicePlanRollupLimitRangeRule(64, 80),
            PendingServicePlanRollupAllowedCodeRule(mutableListOf<String>("pending")),
            PendingServicePlanRollupShiftCodeLengthRule(255),
        ),
    )
}
