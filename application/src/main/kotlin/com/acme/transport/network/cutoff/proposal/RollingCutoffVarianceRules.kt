package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize

/** A rule a rolling cutoff variance must pass. Check returns its problems, or none. */
interface RollingCutoffVarianceRule {
    fun check(item: RollingCutoffVariance): MutableList<String>
}

data class RollingCutoffVarianceRequiredDescriptionRule(var message: String) : RollingCutoffVarianceRule {
    override fun check(item: RollingCutoffVariance): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffVarianceAllowedNameRule(var allowed: MutableList<String>) : RollingCutoffVarianceRule {
    override fun check(item: RollingCutoffVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffVarianceRequiredCodeRule(var message: String) : RollingCutoffVarianceRule {
    override fun check(item: RollingCutoffVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffVarianceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingCutoffVarianceRule {
    override fun check(item: RollingCutoffVariance): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff variance. */
data class RollingCutoffVarianceValidator(var rules: MutableList<RollingCutoffVarianceRule>) {
    fun validate(item: RollingCutoffVariance): MutableList<String> {
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

    fun requireValid(item: RollingCutoffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffVarianceValidator(): RollingCutoffVarianceValidator {
    return RollingCutoffVarianceValidator(
        mutableListOf<RollingCutoffVarianceRule>(
            RollingCutoffVarianceRequiredDescriptionRule("rolling cutoff variance needs an entry"),
            RollingCutoffVarianceAllowedNameRule(mutableListOf<String>("strict", "tiered")),
            RollingCutoffVarianceRequiredCodeRule("rolling cutoff variance needs this field"),
            RollingCutoffVarianceThresholdRangeRule(0.8, 2166.3),
        ),
    )
}
