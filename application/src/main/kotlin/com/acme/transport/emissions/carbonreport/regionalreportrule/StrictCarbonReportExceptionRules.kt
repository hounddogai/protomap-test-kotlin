package com.acme.transport.emissions.carbonreport.regionalreportrule

/** A rule a strict carbon report exception must pass. Check returns its problems, or none. */
interface StrictCarbonReportExceptionRule {
    fun check(item: StrictCarbonReportException): MutableList<String>
}

data class StrictCarbonReportExceptionLabelLengthRule(var maxLength: Int) : StrictCarbonReportExceptionRule {
    override fun check(item: StrictCarbonReportException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportExceptionRequiredLabelRule(var message: String) : StrictCarbonReportExceptionRule {
    override fun check(item: StrictCarbonReportException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCarbonReportExceptionRevisionRangeRule(
    var min: Int,
    var max: Int,
) : StrictCarbonReportExceptionRule {
    override fun check(item: StrictCarbonReportException): MutableList<String> {
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

data class StrictCarbonReportExceptionMaximumRangeRule(
    var min: Double,
    var max: Double,
) : StrictCarbonReportExceptionRule {
    override fun check(item: StrictCarbonReportException): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class StrictCarbonReportExceptionWeightRangeRule(
    var min: Double,
    var max: Double,
) : StrictCarbonReportExceptionRule {
    override fun check(item: StrictCarbonReportException): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict carbon report exception. */
data class StrictCarbonReportExceptionValidator(var rules: MutableList<StrictCarbonReportExceptionRule>) {
    fun validate(item: StrictCarbonReportException): MutableList<String> {
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

    fun requireValid(item: StrictCarbonReportException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCarbonReportException>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCarbonReportExceptionValidator(): StrictCarbonReportExceptionValidator {
    return StrictCarbonReportExceptionValidator(
        mutableListOf<StrictCarbonReportExceptionRule>(
            StrictCarbonReportExceptionLabelLengthRule(3600),
            StrictCarbonReportExceptionRequiredLabelRule("strict carbon report exception needs an entry"),
            StrictCarbonReportExceptionRevisionRangeRule(120, 3600),
            StrictCarbonReportExceptionMaximumRangeRule(9.5, 239.7),
            StrictCarbonReportExceptionWeightRangeRule(0.9, 1965.4),
        ),
    )
}
