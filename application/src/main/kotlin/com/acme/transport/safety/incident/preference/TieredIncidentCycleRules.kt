package com.acme.transport.safety.incident.preference

/** A rule a tiered incident cycle must pass. Check returns its problems, or none. */
interface TieredIncidentCycleRule {
    fun check(item: TieredIncidentCycle): MutableList<String>
}

data class TieredIncidentCycleQuantityRangeRule(var min: Int, var max: Int) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentCycleCodePrefixRule(var prefix: String) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentCycleMinimumRangeRule(var min: Double, var max: Double) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class TieredIncidentCycleNameLengthRule(var maxLength: Int) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentCycleRequiredLaneRule(var message: String) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentCycleLaneLengthRule(var maxLength: Int) : TieredIncidentCycleRule {
    override fun check(item: TieredIncidentCycle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered incident cycle. */
data class TieredIncidentCycleValidator(var rules: MutableList<TieredIncidentCycleRule>) {
    fun validate(item: TieredIncidentCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredIncidentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIncidentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIncidentCycleValidator(): TieredIncidentCycleValidator {
    return TieredIncidentCycleValidator(
        mutableListOf<TieredIncidentCycleRule>(
            TieredIncidentCycleQuantityRangeRule(16, 86400),
            TieredIncidentCycleCodePrefixRule("Q"),
            TieredIncidentCycleMinimumRangeRule(7.5, 4709.3),
            TieredIncidentCycleNameLengthRule(255),
            TieredIncidentCycleRequiredLaneRule("tiered incident cycle needs an entry"),
            TieredIncidentCycleLaneLengthRule(16),
        ),
    )
}
