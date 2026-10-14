package com.acme.transport.emissions.idling.target

/** A rule a idling threshold must pass. Check returns its problems, or none. */
interface IdlingThresholdRule {
    fun check(item: IdlingThreshold): MutableList<String>
}

data class IdlingThresholdRateRangeRule(var min: Double, var max: Double) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingThresholdRequiredCodeRule(var message: String) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingThresholdPriorityRangeRule(var min: Int, var max: Int) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class IdlingThresholdGraceDaysRangeRule(var min: Int, var max: Int) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class IdlingThresholdCodeLengthRule(var maxLength: Int) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingThresholdRequiredExternalRefRule(var message: String) : IdlingThresholdRule {
    override fun check(item: IdlingThreshold): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling threshold. */
data class IdlingThresholdValidator(var rules: MutableList<IdlingThresholdRule>) {
    fun validate(item: IdlingThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingThresholdValidator(): IdlingThresholdValidator {
    return IdlingThresholdValidator(
        mutableListOf<IdlingThresholdRule>(
            IdlingThresholdRateRangeRule(0.9, 932.4),
            IdlingThresholdRequiredCodeRule("idling threshold needs an entry"),
            IdlingThresholdPriorityRangeRule(16, 80),
            IdlingThresholdGraceDaysRangeRule(255, 86400),
            IdlingThresholdCodeLengthRule(255),
            IdlingThresholdRequiredExternalRefRule("idling threshold needs this field"),
        ),
    )
}
