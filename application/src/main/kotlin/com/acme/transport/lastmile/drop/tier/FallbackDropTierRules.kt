package com.acme.transport.lastmile.drop.tier

/** A rule a fallback drop tier must pass. Check returns its problems, or none. */
interface FallbackDropTierRule {
    fun check(item: FallbackDropTier): MutableList<String>
}

data class FallbackDropTierCodeLengthRule(var maxLength: Int) : FallbackDropTierRule {
    override fun check(item: FallbackDropTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropTierRequiredRegionRule(var message: String) : FallbackDropTierRule {
    override fun check(item: FallbackDropTier): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropTierOdometerRangeRule(var min: Int, var max: Int) : FallbackDropTierRule {
    override fun check(item: FallbackDropTier): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropTierRequiredLabelRule(var message: String) : FallbackDropTierRule {
    override fun check(item: FallbackDropTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback drop tier. */
data class FallbackDropTierValidator(var rules: MutableList<FallbackDropTierRule>) {
    fun validate(item: FallbackDropTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDropTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDropTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDropTierValidator(): FallbackDropTierValidator {
    return FallbackDropTierValidator(
        mutableListOf<FallbackDropTierRule>(
            FallbackDropTierCodeLengthRule(1000),
            FallbackDropTierRequiredRegionRule("fallback drop tier needs an entry"),
            FallbackDropTierOdometerRangeRule(16, 86400),
            FallbackDropTierRequiredLabelRule("fallback drop tier needs an entry"),
        ),
    )
}
