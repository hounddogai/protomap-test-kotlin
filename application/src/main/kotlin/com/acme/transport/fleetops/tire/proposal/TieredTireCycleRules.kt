package com.acme.transport.fleetops.tire.proposal

/** A rule a tiered tire cycle must pass. Check returns its problems, or none. */
interface TieredTireCycleRule {
    fun check(item: TieredTireCycle): MutableList<String>
}

data class TieredTireCycleRequiredCodeRule(var message: String) : TieredTireCycleRule {
    override fun check(item: TieredTireCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTireCycleLaneLengthRule(var maxLength: Int) : TieredTireCycleRule {
    override fun check(item: TieredTireCycle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireCycleVersionRangeRule(var min: Int, var max: Int) : TieredTireCycleRule {
    override fun check(item: TieredTireCycle): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class TieredTireCycleEndsAtRangeRule(var min: Int, var max: Int) : TieredTireCycleRule {
    override fun check(item: TieredTireCycle): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered tire cycle. */
data class TieredTireCycleValidator(var rules: MutableList<TieredTireCycleRule>) {
    fun validate(item: TieredTireCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireCycleValidator(): TieredTireCycleValidator {
    return TieredTireCycleValidator(
        mutableListOf<TieredTireCycleRule>(
            TieredTireCycleRequiredCodeRule("tiered tire cycle needs a value"),
            TieredTireCycleLaneLengthRule(120),
            TieredTireCycleVersionRangeRule(80, 3600),
            TieredTireCycleEndsAtRangeRule(32, 64),
        ),
    )
}
