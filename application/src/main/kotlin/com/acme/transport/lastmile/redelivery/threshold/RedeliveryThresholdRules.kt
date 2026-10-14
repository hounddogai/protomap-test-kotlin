package com.acme.transport.lastmile.redelivery.threshold

/** A rule a redelivery threshold must pass. Check returns its problems, or none. */
interface RedeliveryThresholdRule {
    fun check(item: RedeliveryThreshold): MutableList<String>
}

data class RedeliveryThresholdRequiredCodeRule(var message: String) : RedeliveryThresholdRule {
    override fun check(item: RedeliveryThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryThresholdRequiredNameRule(var message: String) : RedeliveryThresholdRule {
    override fun check(item: RedeliveryThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryThresholdFloorValueRangeRule(var min: Double, var max: Double) : RedeliveryThresholdRule {
    override fun check(item: RedeliveryThreshold): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery threshold. */
data class RedeliveryThresholdValidator(var rules: MutableList<RedeliveryThresholdRule>) {
    fun validate(item: RedeliveryThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryThresholdValidator(): RedeliveryThresholdValidator {
    return RedeliveryThresholdValidator(
        mutableListOf<RedeliveryThresholdRule>(
            RedeliveryThresholdRequiredCodeRule("redelivery threshold needs an entry"),
            RedeliveryThresholdRequiredNameRule("redelivery threshold needs a value"),
            RedeliveryThresholdFloorValueRangeRule(6.1, 4459.7),
        ),
    )
}
