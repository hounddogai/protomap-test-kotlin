package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.normalize

/** A rule a regional carbon report manifest must pass. Check returns its problems, or none. */
interface RegionalCarbonReportManifestRule {
    fun check(item: RegionalCarbonReportManifest): MutableList<String>
}

data class RegionalCarbonReportManifestRequiredCodeRule(
    var message: String,
) : RegionalCarbonReportManifestRule {
    override fun check(item: RegionalCarbonReportManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportManifestTitleLengthRule(var maxLength: Int) : RegionalCarbonReportManifestRule {
    override fun check(item: RegionalCarbonReportManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportManifestRequiredCarrierRule(
    var message: String,
) : RegionalCarbonReportManifestRule {
    override fun check(item: RegionalCarbonReportManifest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalCarbonReportManifestRule {
    override fun check(item: RegionalCarbonReportManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportManifestCodePrefixRule(var prefix: String) : RegionalCarbonReportManifestRule {
    override fun check(item: RegionalCarbonReportManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional carbon report manifest. */
data class RegionalCarbonReportManifestValidator(var rules: MutableList<RegionalCarbonReportManifestRule>) {
    fun validate(item: RegionalCarbonReportManifest): MutableList<String> {
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

    fun requireValid(item: RegionalCarbonReportManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportManifestValidator(): RegionalCarbonReportManifestValidator {
    return RegionalCarbonReportManifestValidator(
        mutableListOf<RegionalCarbonReportManifestRule>(
            RegionalCarbonReportManifestRequiredCodeRule("regional carbon report manifest needs an entry"),
            RegionalCarbonReportManifestTitleLengthRule(255),
            RegionalCarbonReportManifestRequiredCarrierRule("regional carbon report manifest needs this field"),
            RegionalCarbonReportManifestAllowedCodeRule(mutableListOf<String>("adaptive", "pending", "capped")),
            RegionalCarbonReportManifestCodePrefixRule("REF"),
        ),
    )
}
