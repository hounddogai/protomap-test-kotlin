package com.acme.transport.fleetops.tire.mapping

/** A rule a tire target must pass. Check returns its problems, or none. */
interface TireTargetRule {
    fun check(item: TireTarget): MutableList<String>
}

data class TireTargetAttemptsRangeRule(var min: Int, var max: Int) : TireTargetRule {
    override fun check(item: TireTarget): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class TireTargetUnitLengthRule(var maxLength: Int) : TireTargetRule {
    override fun check(item: TireTarget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireTargetRateRangeRule(var min: Double, var max: Double) : TireTargetRule {
    override fun check(item: TireTarget): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class TireTargetRegionLengthRule(var maxLength: Int) : TireTargetRule {
    override fun check(item: TireTarget): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire target. */
data class TireTargetValidator(var rules: MutableList<TireTargetRule>) {
    fun validate(item: TireTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTireTargetValidator(): TireTargetValidator {
    return TireTargetValidator(
        mutableListOf<TireTargetRule>(
            TireTargetAttemptsRangeRule(64, 120),
            TireTargetUnitLengthRule(86400),
            TireTargetRateRangeRule(1.6, 3541.3),
            TireTargetRegionLengthRule(86400),
        ),
    )
}
