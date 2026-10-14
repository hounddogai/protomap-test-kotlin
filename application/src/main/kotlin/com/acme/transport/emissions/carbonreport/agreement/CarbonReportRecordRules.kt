package com.acme.transport.emissions.carbonreport.agreement

/** A rule a carbon report record must pass. Check returns its problems, or none. */
interface CarbonReportRecordRule {
    fun check(item: CarbonReportRecord): MutableList<String>
}

data class CarbonReportRecordRequiredReviewerRule(var message: String) : CarbonReportRecordRule {
    override fun check(item: CarbonReportRecord): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRecordDriverIdLengthRule(var maxLength: Int) : CarbonReportRecordRule {
    override fun check(item: CarbonReportRecord): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRecordStartsAtRangeRule(var min: Int, var max: Int) : CarbonReportRecordRule {
    override fun check(item: CarbonReportRecord): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRecordThresholdRangeRule(var min: Double, var max: Double) : CarbonReportRecordRule {
    override fun check(item: CarbonReportRecord): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report record. */
data class CarbonReportRecordValidator(var rules: MutableList<CarbonReportRecordRule>) {
    fun validate(item: CarbonReportRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportRecordValidator(): CarbonReportRecordValidator {
    return CarbonReportRecordValidator(
        mutableListOf<CarbonReportRecordRule>(
            CarbonReportRecordRequiredReviewerRule("carbon report record needs this field"),
            CarbonReportRecordDriverIdLengthRule(80),
            CarbonReportRecordStartsAtRangeRule(120, 255),
            CarbonReportRecordThresholdRangeRule(3.6, 1512.5),
        ),
    )
}
