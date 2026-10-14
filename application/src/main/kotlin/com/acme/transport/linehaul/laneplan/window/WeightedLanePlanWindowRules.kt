package com.acme.transport.linehaul.laneplan.window

/** A rule a weighted lane plan window must pass. Check returns its problems, or none. */
interface WeightedLanePlanWindowRule {
    fun check(item: WeightedLanePlanWindow): MutableList<String>
}

data class WeightedLanePlanWindowWeightRangeRule(
    var min: Double,
    var max: Double,
) : WeightedLanePlanWindowRule {
    override fun check(item: WeightedLanePlanWindow): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanWindowCodePrefixRule(var prefix: String) : WeightedLanePlanWindowRule {
    override fun check(item: WeightedLanePlanWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanWindowContactEmailLengthRule(var maxLength: Int) : WeightedLanePlanWindowRule {
    override fun check(item: WeightedLanePlanWindow): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanWindowNameLengthRule(var maxLength: Int) : WeightedLanePlanWindowRule {
    override fun check(item: WeightedLanePlanWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan window. */
data class WeightedLanePlanWindowValidator(var rules: MutableList<WeightedLanePlanWindowRule>) {
    fun validate(item: WeightedLanePlanWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLanePlanWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanWindowValidator(): WeightedLanePlanWindowValidator {
    return WeightedLanePlanWindowValidator(
        mutableListOf<WeightedLanePlanWindowRule>(
            WeightedLanePlanWindowWeightRangeRule(2.3, 4511.1),
            WeightedLanePlanWindowCodePrefixRule("ACM"),
            WeightedLanePlanWindowContactEmailLengthRule(255),
            WeightedLanePlanWindowNameLengthRule(1000),
        ),
    )
}
