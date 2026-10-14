package com.acme.transport.emissions.carbonreport.entry

/** A rule a fallback carbon report record must pass. Check returns its problems, or none. */
interface FallbackCarbonReportRecordRule {
    fun check(item: FallbackCarbonReportRecord): MutableList<String>
}

data class FallbackCarbonReportRecordDockLengthRule(var maxLength: Int) : FallbackCarbonReportRecordRule {
    override fun check(item: FallbackCarbonReportRecord): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportRecordUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCarbonReportRecordRule {
    override fun check(item: FallbackCarbonReportRecord): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportRecordStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCarbonReportRecordRule {
    override fun check(item: FallbackCarbonReportRecord): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportRecordRequiredLabelRule(var message: String) : FallbackCarbonReportRecordRule {
    override fun check(item: FallbackCarbonReportRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportRecordCodePrefixRule(var prefix: String) : FallbackCarbonReportRecordRule {
    override fun check(item: FallbackCarbonReportRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback carbon report record. */
data class FallbackCarbonReportRecordValidator(var rules: MutableList<FallbackCarbonReportRecordRule>) {
    fun validate(item: FallbackCarbonReportRecord): MutableList<String> {
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

    fun requireValid(item: FallbackCarbonReportRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCarbonReportRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCarbonReportRecordValidator(): FallbackCarbonReportRecordValidator {
    return FallbackCarbonReportRecordValidator(
        mutableListOf<FallbackCarbonReportRecordRule>(
            FallbackCarbonReportRecordDockLengthRule(16),
            FallbackCarbonReportRecordUpdatedAtRangeRule(120, 120),
            FallbackCarbonReportRecordStartsAtRangeRule(16, 120),
            FallbackCarbonReportRecordRequiredLabelRule("fallback carbon report record needs an entry"),
            FallbackCarbonReportRecordCodePrefixRule("REF"),
        ),
    )
}
