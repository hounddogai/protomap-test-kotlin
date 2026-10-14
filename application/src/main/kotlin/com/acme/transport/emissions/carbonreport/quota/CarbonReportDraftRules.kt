package com.acme.transport.emissions.carbonreport.quota

/** A rule a carbon report draft must pass. Check returns its problems, or none. */
interface CarbonReportDraftRule {
    fun check(item: CarbonReportDraft): MutableList<String>
}

data class CarbonReportDraftVersionRangeRule(var min: Int, var max: Int) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class CarbonReportDraftCodePrefixRule(var prefix: String) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportDraftCodeLengthRule(var maxLength: Int) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportDraftRetriesRangeRule(var min: Int, var max: Int) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportDraftNameLengthRule(var maxLength: Int) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportDraftRequiredDepotRule(var message: String) : CarbonReportDraftRule {
    override fun check(item: CarbonReportDraft): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report draft. */
data class CarbonReportDraftValidator(var rules: MutableList<CarbonReportDraftRule>) {
    fun validate(item: CarbonReportDraft): MutableList<String> {
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

    fun requireValid(item: CarbonReportDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportDraftValidator(): CarbonReportDraftValidator {
    return CarbonReportDraftValidator(
        mutableListOf<CarbonReportDraftRule>(
            CarbonReportDraftVersionRangeRule(1000, 86400),
            CarbonReportDraftCodePrefixRule("ACM"),
            CarbonReportDraftCodeLengthRule(255),
            CarbonReportDraftRetriesRangeRule(1000, 86400),
            CarbonReportDraftNameLengthRule(1000),
            CarbonReportDraftRequiredDepotRule("carbon report draft needs a value"),
        ),
    )
}
