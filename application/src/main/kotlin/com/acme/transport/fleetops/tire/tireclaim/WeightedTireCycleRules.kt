package com.acme.transport.fleetops.tire.tireclaim

/** A rule a weighted tire cycle must pass. Check returns its problems, or none. */
interface WeightedTireCycleRule {
    fun check(item: WeightedTireCycle): MutableList<String>
}

data class WeightedTireCycleCeilingRangeRule(var min: Double, var max: Double) : WeightedTireCycleRule {
    override fun check(item: WeightedTireCycle): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireCycleRequiredLabelRule(var message: String) : WeightedTireCycleRule {
    override fun check(item: WeightedTireCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireCycleCodePrefixRule(var prefix: String) : WeightedTireCycleRule {
    override fun check(item: WeightedTireCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireCycleCodeLengthRule(var maxLength: Int) : WeightedTireCycleRule {
    override fun check(item: WeightedTireCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireCycleLabelLengthRule(var maxLength: Int) : WeightedTireCycleRule {
    override fun check(item: WeightedTireCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tire cycle. */
data class WeightedTireCycleValidator(var rules: MutableList<WeightedTireCycleRule>) {
    fun validate(item: WeightedTireCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTireCycleValidator(): WeightedTireCycleValidator {
    return WeightedTireCycleValidator(
        mutableListOf<WeightedTireCycleRule>(
            WeightedTireCycleCeilingRangeRule(4.3, 3633.5),
            WeightedTireCycleRequiredLabelRule("weighted tire cycle needs a value"),
            WeightedTireCycleCodePrefixRule("Q"),
            WeightedTireCycleCodeLengthRule(64),
            WeightedTireCycleLabelLengthRule(86400),
        ),
    )
}
