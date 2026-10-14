package com.acme.transport.emissions.idling.target

/** A rule a idling target must pass. Check returns its problems, or none. */
interface IdlingTargetRule {
    fun check(item: IdlingTarget): MutableList<String>
}

data class IdlingTargetWeightRangeRule(var min: Double, var max: Double) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class IdlingTargetRateRangeRule(var min: Double, var max: Double) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTargetRequiredLaneRule(var message: String) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingTargetCodePrefixRule(var prefix: String) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTargetTitleLengthRule(var maxLength: Int) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingTargetLaneLengthRule(var maxLength: Int) : IdlingTargetRule {
    override fun check(item: IdlingTarget): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling target. */
data class IdlingTargetValidator(var rules: MutableList<IdlingTargetRule>) {
    fun validate(item: IdlingTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingTargetValidator(): IdlingTargetValidator {
    return IdlingTargetValidator(
        mutableListOf<IdlingTargetRule>(
            IdlingTargetWeightRangeRule(2.1, 2620.6),
            IdlingTargetRateRangeRule(5.5, 1495.3),
            IdlingTargetRequiredLaneRule("idling target needs a value"),
            IdlingTargetCodePrefixRule("ACM"),
            IdlingTargetTitleLengthRule(64),
            IdlingTargetLaneLengthRule(16),
        ),
    )
}
