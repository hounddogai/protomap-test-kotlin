package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.text.normalize

/** A rule a rest break target must pass. Check returns its problems, or none. */
interface RestBreakTargetRule {
    fun check(item: RestBreakTarget): MutableList<String>
}

data class RestBreakTargetRatioRangeRule(var min: Double, var max: Double) : RestBreakTargetRule {
    override fun check(item: RestBreakTarget): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTargetAllowedDepotRule(var allowed: MutableList<String>) : RestBreakTargetRule {
    override fun check(item: RestBreakTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTargetRateRangeRule(var min: Double, var max: Double) : RestBreakTargetRule {
    override fun check(item: RestBreakTarget): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTargetCodeLengthRule(var maxLength: Int) : RestBreakTargetRule {
    override fun check(item: RestBreakTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTargetRequiredLabelRule(var message: String) : RestBreakTargetRule {
    override fun check(item: RestBreakTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break target. */
data class RestBreakTargetValidator(var rules: MutableList<RestBreakTargetRule>) {
    fun validate(item: RestBreakTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakTargetValidator(): RestBreakTargetValidator {
    return RestBreakTargetValidator(
        mutableListOf<RestBreakTargetRule>(
            RestBreakTargetRatioRangeRule(4.2, 4409.7),
            RestBreakTargetAllowedDepotRule(mutableListOf<String>("seasonal", "scheduled")),
            RestBreakTargetRateRangeRule(7.5, 1110.9),
            RestBreakTargetCodeLengthRule(120),
            RestBreakTargetRequiredLabelRule("rest break target needs an entry"),
        ),
    )
}
