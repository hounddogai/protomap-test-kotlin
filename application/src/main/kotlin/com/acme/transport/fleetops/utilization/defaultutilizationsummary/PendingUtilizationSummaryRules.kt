package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** A rule a pending utilization summary must pass. Check returns its problems, or none. */
interface PendingUtilizationSummaryRule {
    fun check(item: PendingUtilizationSummary): MutableList<String>
}

data class PendingUtilizationSummaryDockLengthRule(var maxLength: Int) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationSummaryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
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

data class PendingUtilizationSummaryCodeLengthRule(var maxLength: Int) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationSummaryFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
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

data class PendingUtilizationSummaryActiveGuardRule(var limit: Double) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
        if (item.active && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationSummaryRequiredCodeRule(var message: String) : PendingUtilizationSummaryRule {
    override fun check(item: PendingUtilizationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending utilization summary. */
data class PendingUtilizationSummaryValidator(var rules: MutableList<PendingUtilizationSummaryRule>) {
    fun validate(item: PendingUtilizationSummary): MutableList<String> {
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

    fun requireValid(item: PendingUtilizationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingUtilizationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingUtilizationSummaryValidator(): PendingUtilizationSummaryValidator {
    return PendingUtilizationSummaryValidator(
        mutableListOf<PendingUtilizationSummaryRule>(
            PendingUtilizationSummaryDockLengthRule(255),
            PendingUtilizationSummaryFuelLitersRangeRule(2.1, 1489.2),
            PendingUtilizationSummaryCodeLengthRule(32),
            PendingUtilizationSummaryFloorValueRangeRule(6.7, 1470.8),
            PendingUtilizationSummaryActiveGuardRule(8.7),
            PendingUtilizationSummaryRequiredCodeRule("pending utilization summary needs this field"),
        ),
    )
}
