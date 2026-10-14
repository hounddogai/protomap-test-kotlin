package com.acme.transport.safety.coaching.proposal

/** A rule a weighted coaching bundle must pass. Check returns its problems, or none. */
interface WeightedCoachingBundleRule {
    fun check(item: WeightedCoachingBundle): MutableList<String>
}

data class WeightedCoachingBundleCodeLengthRule(var maxLength: Int) : WeightedCoachingBundleRule {
    override fun check(item: WeightedCoachingBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCoachingBundleCodePrefixRule(var prefix: String) : WeightedCoachingBundleRule {
    override fun check(item: WeightedCoachingBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCoachingBundleRequiredTitleRule(var message: String) : WeightedCoachingBundleRule {
    override fun check(item: WeightedCoachingBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCoachingBundleWindowDaysRangeRule(var min: Int, var max: Int) : WeightedCoachingBundleRule {
    override fun check(item: WeightedCoachingBundle): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCoachingBundleRequiredVehicleIdRule(var message: String) : WeightedCoachingBundleRule {
    override fun check(item: WeightedCoachingBundle): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted coaching bundle. */
data class WeightedCoachingBundleValidator(var rules: MutableList<WeightedCoachingBundleRule>) {
    fun validate(item: WeightedCoachingBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCoachingBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCoachingBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCoachingBundleValidator(): WeightedCoachingBundleValidator {
    return WeightedCoachingBundleValidator(
        mutableListOf<WeightedCoachingBundleRule>(
            WeightedCoachingBundleCodeLengthRule(86400),
            WeightedCoachingBundleCodePrefixRule("ACM"),
            WeightedCoachingBundleRequiredTitleRule("weighted coaching bundle needs this field"),
            WeightedCoachingBundleWindowDaysRangeRule(120, 255),
            WeightedCoachingBundleRequiredVehicleIdRule("weighted coaching bundle needs this field"),
        ),
    )
}
