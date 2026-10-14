package com.acme.transport.terminal.dockdoor.estimate

/** A rule a legacy dock door metric must pass. Check returns its problems, or none. */
interface LegacyDockDoorMetricRule {
    fun check(item: LegacyDockDoorMetric): MutableList<String>
}

data class LegacyDockDoorMetricCodeLengthRule(var maxLength: Int) : LegacyDockDoorMetricRule {
    override fun check(item: LegacyDockDoorMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorMetricAmountRangeRule(var min: Double, var max: Double) : LegacyDockDoorMetricRule {
    override fun check(item: LegacyDockDoorMetric): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class LegacyDockDoorMetricRequiredShiftCodeRule(var message: String) : LegacyDockDoorMetricRule {
    override fun check(item: LegacyDockDoorMetric): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorMetricRegionLengthRule(var maxLength: Int) : LegacyDockDoorMetricRule {
    override fun check(item: LegacyDockDoorMetric): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorMetricShiftCodeLengthRule(var maxLength: Int) : LegacyDockDoorMetricRule {
    override fun check(item: LegacyDockDoorMetric): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dock door metric. */
data class LegacyDockDoorMetricValidator(var rules: MutableList<LegacyDockDoorMetricRule>) {
    fun validate(item: LegacyDockDoorMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDockDoorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDockDoorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDockDoorMetricValidator(): LegacyDockDoorMetricValidator {
    return LegacyDockDoorMetricValidator(
        mutableListOf<LegacyDockDoorMetricRule>(
            LegacyDockDoorMetricCodeLengthRule(1000),
            LegacyDockDoorMetricAmountRangeRule(4.2, 607.7),
            LegacyDockDoorMetricRequiredShiftCodeRule("legacy dock door metric needs this field"),
            LegacyDockDoorMetricRegionLengthRule(255),
            LegacyDockDoorMetricShiftCodeLengthRule(32),
        ),
    )
}
