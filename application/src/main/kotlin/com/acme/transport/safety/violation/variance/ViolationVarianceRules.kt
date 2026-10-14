package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.text.normalize

/** A rule a violation variance must pass. Check returns its problems, or none. */
interface ViolationVarianceRule {
    fun check(item: ViolationVariance): MutableList<String>
}

data class ViolationVarianceRequiredNameRule(var message: String) : ViolationVarianceRule {
    override fun check(item: ViolationVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationVarianceAllowedCarrierRule(var allowed: MutableList<String>) : ViolationVarianceRule {
    override fun check(item: ViolationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ViolationVarianceCodeLengthRule(var maxLength: Int) : ViolationVarianceRule {
    override fun check(item: ViolationVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationVariancePriorityRangeRule(var min: Int, var max: Int) : ViolationVarianceRule {
    override fun check(item: ViolationVariance): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation variance. */
data class ViolationVarianceValidator(var rules: MutableList<ViolationVarianceRule>) {
    fun validate(item: ViolationVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationVarianceValidator(): ViolationVarianceValidator {
    return ViolationVarianceValidator(
        mutableListOf<ViolationVarianceRule>(
            ViolationVarianceRequiredNameRule("violation variance needs a value"),
            ViolationVarianceAllowedCarrierRule(mutableListOf<String>()),
            ViolationVarianceCodeLengthRule(3600),
            ViolationVariancePriorityRangeRule(80, 3600),
        ),
    )
}
