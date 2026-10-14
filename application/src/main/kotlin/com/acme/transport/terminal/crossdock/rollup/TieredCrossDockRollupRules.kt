package com.acme.transport.terminal.crossdock.rollup

/** A rule a tiered cross dock rollup must pass. Check returns its problems, or none. */
interface TieredCrossDockRollupRule {
    fun check(item: TieredCrossDockRollup): MutableList<String>
}

data class TieredCrossDockRollupLocaleLengthRule(var maxLength: Int) : TieredCrossDockRollupRule {
    override fun check(item: TieredCrossDockRollup): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRollupRequiredCodeRule(var message: String) : TieredCrossDockRollupRule {
    override fun check(item: TieredCrossDockRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRollupRequiredNameRule(var message: String) : TieredCrossDockRollupRule {
    override fun check(item: TieredCrossDockRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockRollupFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : TieredCrossDockRollupRule {
    override fun check(item: TieredCrossDockRollup): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class TieredCrossDockRollupGraceDaysRangeRule(var min: Int, var max: Int) : TieredCrossDockRollupRule {
    override fun check(item: TieredCrossDockRollup): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock rollup. */
data class TieredCrossDockRollupValidator(var rules: MutableList<TieredCrossDockRollupRule>) {
    fun validate(item: TieredCrossDockRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCrossDockRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockRollupValidator(): TieredCrossDockRollupValidator {
    return TieredCrossDockRollupValidator(
        mutableListOf<TieredCrossDockRollupRule>(
            TieredCrossDockRollupLocaleLengthRule(16),
            TieredCrossDockRollupRequiredCodeRule("tiered cross dock rollup needs a value"),
            TieredCrossDockRollupRequiredNameRule("tiered cross dock rollup needs a value"),
            TieredCrossDockRollupFuelLitersRangeRule(0.7, 2401.2),
            TieredCrossDockRollupGraceDaysRangeRule(120, 1000),
        ),
    )
}
