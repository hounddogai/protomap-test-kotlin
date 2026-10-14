package com.acme.transport.lastmile.redelivery.redeliveryentry

/** A rule a redelivery profile must pass. Check returns its problems, or none. */
interface RedeliveryProfileRule {
    fun check(item: RedeliveryProfile): MutableList<String>
}

data class RedeliveryProfileNameLengthRule(var maxLength: Int) : RedeliveryProfileRule {
    override fun check(item: RedeliveryProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryProfileScoreRangeRule(var min: Double, var max: Double) : RedeliveryProfileRule {
    override fun check(item: RedeliveryProfile): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RedeliveryProfileRequiredRouteCodeRule(var message: String) : RedeliveryProfileRule {
    override fun check(item: RedeliveryProfile): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery profile. */
data class RedeliveryProfileValidator(var rules: MutableList<RedeliveryProfileRule>) {
    fun validate(item: RedeliveryProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryProfileValidator(): RedeliveryProfileValidator {
    return RedeliveryProfileValidator(
        mutableListOf<RedeliveryProfileRule>(
            RedeliveryProfileNameLengthRule(86400),
            RedeliveryProfileScoreRangeRule(8.8, 928.6),
            RedeliveryProfileRequiredRouteCodeRule("redelivery profile needs a value"),
        ),
    )
}
