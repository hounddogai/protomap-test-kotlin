package com.acme.transport.network.node.seasonalnodeagreement

/** A rule a tiered node bundle must pass. Check returns its problems, or none. */
interface TieredNodeBundleRule {
    fun check(item: TieredNodeBundle): MutableList<String>
}

data class TieredNodeBundleLaneLengthRule(var maxLength: Int) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeBundleRateRangeRule(var min: Double, var max: Double) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeBundleRequiredNameRule(var message: String) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeBundleRequiredSegmentRule(var message: String) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNodeBundleMaximumRangeRule(var min: Double, var max: Double) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class TieredNodeBundleRequiredCodeRule(var message: String) : TieredNodeBundleRule {
    override fun check(item: TieredNodeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered node bundle. */
data class TieredNodeBundleValidator(var rules: MutableList<TieredNodeBundleRule>) {
    fun validate(item: TieredNodeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredNodeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNodeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNodeBundleValidator(): TieredNodeBundleValidator {
    return TieredNodeBundleValidator(
        mutableListOf<TieredNodeBundleRule>(
            TieredNodeBundleLaneLengthRule(32),
            TieredNodeBundleRateRangeRule(4.5, 3598.3),
            TieredNodeBundleRequiredNameRule("tiered node bundle needs a value"),
            TieredNodeBundleRequiredSegmentRule("tiered node bundle needs an entry"),
            TieredNodeBundleMaximumRangeRule(0.5, 1610.2),
            TieredNodeBundleRequiredCodeRule("tiered node bundle needs this field"),
        ),
    )
}
