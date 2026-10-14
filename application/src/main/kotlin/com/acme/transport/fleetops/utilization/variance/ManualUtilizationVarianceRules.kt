package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.text.normalize

/** A rule a manual utilization variance must pass. Check returns its problems, or none. */
interface ManualUtilizationVarianceRule {
    fun check(item: ManualUtilizationVariance): MutableList<String>
}

data class ManualUtilizationVarianceCodePrefixRule(var prefix: String) : ManualUtilizationVarianceRule {
    override fun check(item: ManualUtilizationVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationVarianceAllowedDockRule(
    var allowed: MutableList<String>,
) : ManualUtilizationVarianceRule {
    override fun check(item: ManualUtilizationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationVarianceRequiredLabelRule(var message: String) : ManualUtilizationVarianceRule {
    override fun check(item: ManualUtilizationVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationVarianceActiveGuardRule(var limit: Int) : ManualUtilizationVarianceRule {
    override fun check(item: ManualUtilizationVariance): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationVarianceExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualUtilizationVarianceRule {
    override fun check(item: ManualUtilizationVariance): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual utilization variance. */
data class ManualUtilizationVarianceValidator(var rules: MutableList<ManualUtilizationVarianceRule>) {
    fun validate(item: ManualUtilizationVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualUtilizationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationVarianceValidator(): ManualUtilizationVarianceValidator {
    return ManualUtilizationVarianceValidator(
        mutableListOf<ManualUtilizationVarianceRule>(
            ManualUtilizationVarianceCodePrefixRule("OPS"),
            ManualUtilizationVarianceAllowedDockRule(mutableListOf<String>("capped", "batched")),
            ManualUtilizationVarianceRequiredLabelRule("manual utilization variance needs an entry"),
            ManualUtilizationVarianceActiveGuardRule(80),
            ManualUtilizationVarianceExpiresAtRangeRule(120, 1000),
        ),
    )
}
