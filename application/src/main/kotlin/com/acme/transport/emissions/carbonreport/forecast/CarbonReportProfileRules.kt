package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.normalize

/** A rule a carbon report profile must pass. Check returns its problems, or none. */
interface CarbonReportProfileRule {
    fun check(item: CarbonReportProfile): MutableList<String>
}

data class CarbonReportProfileAllowedTitleRule(var allowed: MutableList<String>) : CarbonReportProfileRule {
    override fun check(item: CarbonReportProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportProfileCodeLengthRule(var maxLength: Int) : CarbonReportProfileRule {
    override fun check(item: CarbonReportProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportProfileRequiredOwnerIdRule(var message: String) : CarbonReportProfileRule {
    override fun check(item: CarbonReportProfile): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportProfileRequiredRegionRule(var message: String) : CarbonReportProfileRule {
    override fun check(item: CarbonReportProfile): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report profile. */
data class CarbonReportProfileValidator(var rules: MutableList<CarbonReportProfileRule>) {
    fun validate(item: CarbonReportProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportProfileValidator(): CarbonReportProfileValidator {
    return CarbonReportProfileValidator(
        mutableListOf<CarbonReportProfileRule>(
            CarbonReportProfileAllowedTitleRule(mutableListOf<String>("legacy", "pending", "legacy")),
            CarbonReportProfileCodeLengthRule(32),
            CarbonReportProfileRequiredOwnerIdRule("carbon report profile needs an entry"),
            CarbonReportProfileRequiredRegionRule("carbon report profile needs this field"),
        ),
    )
}
