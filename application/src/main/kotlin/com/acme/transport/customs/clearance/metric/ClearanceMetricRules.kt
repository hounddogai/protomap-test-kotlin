package com.acme.transport.customs.clearance.metric

/** A rule a clearance metric must pass. Check returns its problems, or none. */
interface ClearanceMetricRule {
    fun check(item: ClearanceMetric): MutableList<String>
}

data class ClearanceMetricRevisionRangeRule(var min: Int, var max: Int) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ClearanceMetricVehicleIdLengthRule(var maxLength: Int) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMetricThresholdRangeRule(var min: Double, var max: Double) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ClearanceMetricCodeLengthRule(var maxLength: Int) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMetricTitleLengthRule(var maxLength: Int) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceMetricStopCountRangeRule(var min: Int, var max: Int) : ClearanceMetricRule {
    override fun check(item: ClearanceMetric): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance metric. */
data class ClearanceMetricValidator(var rules: MutableList<ClearanceMetricRule>) {
    fun validate(item: ClearanceMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceMetricValidator(): ClearanceMetricValidator {
    return ClearanceMetricValidator(
        mutableListOf<ClearanceMetricRule>(
            ClearanceMetricRevisionRangeRule(1000, 86400),
            ClearanceMetricVehicleIdLengthRule(32),
            ClearanceMetricThresholdRangeRule(8.5, 1297.7),
            ClearanceMetricCodeLengthRule(64),
            ClearanceMetricTitleLengthRule(3600),
            ClearanceMetricStopCountRangeRule(80, 1000),
        ),
    )
}
