package com.acme.transport.lastmile.parcel.parcelreservation

/** A rule a weighted parcel override must pass. Check returns its problems, or none. */
interface WeightedParcelOverrideRule {
    fun check(item: WeightedParcelOverride): MutableList<String>
}

data class WeightedParcelOverridePalletsRangeRule(var min: Int, var max: Int) : WeightedParcelOverrideRule {
    override fun check(item: WeightedParcelOverride): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class WeightedParcelOverrideRequiredCodeRule(var message: String) : WeightedParcelOverrideRule {
    override fun check(item: WeightedParcelOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelOverrideCodePrefixRule(var prefix: String) : WeightedParcelOverrideRule {
    override fun check(item: WeightedParcelOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelOverrideVisibleGuardRule(var limit: Int) : WeightedParcelOverrideRule {
    override fun check(item: WeightedParcelOverride): MutableList<String> {
        if (item.visible && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted parcel override. */
data class WeightedParcelOverrideValidator(var rules: MutableList<WeightedParcelOverrideRule>) {
    fun validate(item: WeightedParcelOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedParcelOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelOverrideValidator(): WeightedParcelOverrideValidator {
    return WeightedParcelOverrideValidator(
        mutableListOf<WeightedParcelOverrideRule>(
            WeightedParcelOverridePalletsRangeRule(16, 255),
            WeightedParcelOverrideRequiredCodeRule("weighted parcel override needs a value"),
            WeightedParcelOverrideCodePrefixRule("ACM"),
            WeightedParcelOverrideVisibleGuardRule(86400),
        ),
    )
}
