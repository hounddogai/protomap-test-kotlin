package com.acme.transport.emissions.evcharge.baseline

/** A rule a weighted ev charge group must pass. Check returns its problems, or none. */
interface WeightedEvChargeGroupRule {
    fun check(item: WeightedEvChargeGroup): MutableList<String>
}

data class WeightedEvChargeGroupRequiredCodeRule(var message: String) : WeightedEvChargeGroupRule {
    override fun check(item: WeightedEvChargeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeGroupTitleLengthRule(var maxLength: Int) : WeightedEvChargeGroupRule {
    override fun check(item: WeightedEvChargeGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeGroupRequiredTitleRule(var message: String) : WeightedEvChargeGroupRule {
    override fun check(item: WeightedEvChargeGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeGroupGraceDaysRangeRule(var min: Int, var max: Int) : WeightedEvChargeGroupRule {
    override fun check(item: WeightedEvChargeGroup): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge group. */
data class WeightedEvChargeGroupValidator(var rules: MutableList<WeightedEvChargeGroupRule>) {
    fun validate(item: WeightedEvChargeGroup): MutableList<String> {
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

    fun requireValid(item: WeightedEvChargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeGroupValidator(): WeightedEvChargeGroupValidator {
    return WeightedEvChargeGroupValidator(
        mutableListOf<WeightedEvChargeGroupRule>(
            WeightedEvChargeGroupRequiredCodeRule("weighted ev charge group needs an entry"),
            WeightedEvChargeGroupTitleLengthRule(86400),
            WeightedEvChargeGroupRequiredTitleRule("weighted ev charge group needs this field"),
            WeightedEvChargeGroupGraceDaysRangeRule(32, 120),
        ),
    )
}
