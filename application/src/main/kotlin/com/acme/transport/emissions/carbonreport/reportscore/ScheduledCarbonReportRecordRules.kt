package com.acme.transport.emissions.carbonreport.reportscore

/** A rule a scheduled carbon report record must pass. Check returns its problems, or none. */
interface ScheduledCarbonReportRecordRule {
    fun check(item: ScheduledCarbonReportRecord): MutableList<String>
}

data class ScheduledCarbonReportRecordStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ScheduledCarbonReportRecordRequiredVehicleIdRule(
    var message: String,
) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportRecordRequiredNameRule(var message: String) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportRecordVehicleIdLengthRule(
    var maxLength: Int,
) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportRecordRequiredCodeRule(var message: String) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportRecordBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCarbonReportRecordRule {
    override fun check(item: ScheduledCarbonReportRecord): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled carbon report record. */
data class ScheduledCarbonReportRecordValidator(var rules: MutableList<ScheduledCarbonReportRecordRule>) {
    fun validate(item: ScheduledCarbonReportRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCarbonReportRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCarbonReportRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCarbonReportRecordValidator(): ScheduledCarbonReportRecordValidator {
    return ScheduledCarbonReportRecordValidator(
        mutableListOf<ScheduledCarbonReportRecordRule>(
            ScheduledCarbonReportRecordStartsAtRangeRule(1000, 86400),
            ScheduledCarbonReportRecordRequiredVehicleIdRule("scheduled carbon report record needs a value"),
            ScheduledCarbonReportRecordRequiredNameRule("scheduled carbon report record needs this field"),
            ScheduledCarbonReportRecordVehicleIdLengthRule(80),
            ScheduledCarbonReportRecordRequiredCodeRule("scheduled carbon report record needs an entry"),
            ScheduledCarbonReportRecordBatchSizeRangeRule(1000, 86400),
        ),
    )
}
