package com.acme.transport.emissions.carbonreport.reportrule

/** A rule a carbon report exception must pass. Check returns its problems, or none. */
interface CarbonReportExceptionRule {
    fun check(item: CarbonReportException): MutableList<String>
}

data class CarbonReportExceptionRequiredCodeRule(var message: String) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportExceptionRateRangeRule(var min: Double, var max: Double) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
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

data class CarbonReportExceptionCodeLengthRule(var maxLength: Int) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportExceptionLabelLengthRule(var maxLength: Int) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportExceptionRequiredPhoneRule(var message: String) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportExceptionLoadKgRangeRule(var min: Double, var max: Double) : CarbonReportExceptionRule {
    override fun check(item: CarbonReportException): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report exception. */
data class CarbonReportExceptionValidator(var rules: MutableList<CarbonReportExceptionRule>) {
    fun validate(item: CarbonReportException): MutableList<String> {
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

    fun requireValid(item: CarbonReportException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportException>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportExceptionValidator(): CarbonReportExceptionValidator {
    return CarbonReportExceptionValidator(
        mutableListOf<CarbonReportExceptionRule>(
            CarbonReportExceptionRequiredCodeRule("carbon report exception needs a value"),
            CarbonReportExceptionRateRangeRule(3.7, 2588.6),
            CarbonReportExceptionCodeLengthRule(16),
            CarbonReportExceptionLabelLengthRule(16),
            CarbonReportExceptionRequiredPhoneRule("carbon report exception needs an entry"),
            CarbonReportExceptionLoadKgRangeRule(1.3, 359.8),
        ),
    )
}
