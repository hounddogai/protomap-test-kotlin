package com.acme.transport.freightpay.dispute.reservation

/** A rule a legacy dispute tier must pass. Check returns its problems, or none. */
interface LegacyDisputeTierRule {
    fun check(item: LegacyDisputeTier): MutableList<String>
}

data class LegacyDisputeTierFloorValueRangeRule(var min: Double, var max: Double) : LegacyDisputeTierRule {
    override fun check(item: LegacyDisputeTier): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LegacyDisputeTierStartsAtRangeRule(var min: Int, var max: Int) : LegacyDisputeTierRule {
    override fun check(item: LegacyDisputeTier): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeTierCreatedAtRangeRule(var min: Int, var max: Int) : LegacyDisputeTierRule {
    override fun check(item: LegacyDisputeTier): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy dispute tier. */
data class LegacyDisputeTierValidator(var rules: MutableList<LegacyDisputeTierRule>) {
    fun validate(item: LegacyDisputeTier): MutableList<String> {
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

    fun requireValid(item: LegacyDisputeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDisputeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDisputeTierValidator(): LegacyDisputeTierValidator {
    return LegacyDisputeTierValidator(
        mutableListOf<LegacyDisputeTierRule>(
            LegacyDisputeTierFloorValueRangeRule(2.8, 1584.6),
            LegacyDisputeTierStartsAtRangeRule(120, 120),
            LegacyDisputeTierCreatedAtRangeRule(32, 255),
        ),
    )
}
