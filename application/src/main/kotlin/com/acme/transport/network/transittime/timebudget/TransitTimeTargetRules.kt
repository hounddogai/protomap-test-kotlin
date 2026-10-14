package com.acme.transport.network.transittime.timebudget

/** A rule a transit time target must pass. Check returns its problems, or none. */
interface TransitTimeTargetRule {
    fun check(item: TransitTimeTarget): MutableList<String>
}

data class TransitTimeTargetCodeLengthRule(var maxLength: Int) : TransitTimeTargetRule {
    override fun check(item: TransitTimeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTargetQuantityRangeRule(var min: Int, var max: Int) : TransitTimeTargetRule {
    override fun check(item: TransitTimeTarget): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class TransitTimeTargetUnitLengthRule(var maxLength: Int) : TransitTimeTargetRule {
    override fun check(item: TransitTimeTarget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeTargetScoreRangeRule(var min: Double, var max: Double) : TransitTimeTargetRule {
    override fun check(item: TransitTimeTarget): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time target. */
data class TransitTimeTargetValidator(var rules: MutableList<TransitTimeTargetRule>) {
    fun validate(item: TransitTimeTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeTargetValidator(): TransitTimeTargetValidator {
    return TransitTimeTargetValidator(
        mutableListOf<TransitTimeTargetRule>(
            TransitTimeTargetCodeLengthRule(120),
            TransitTimeTargetQuantityRangeRule(1000, 3600),
            TransitTimeTargetUnitLengthRule(64),
            TransitTimeTargetScoreRangeRule(3.9, 2419.4),
        ),
    )
}
