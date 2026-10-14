package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.normalize

/** A rule a carbon report bundle must pass. Check returns its problems, or none. */
interface CarbonReportBundleRule {
    fun check(item: CarbonReportBundle): MutableList<String>
}

data class CarbonReportBundleRequiredChannelRule(var message: String) : CarbonReportBundleRule {
    override fun check(item: CarbonReportBundle): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBundleRequiredCodeRule(var message: String) : CarbonReportBundleRule {
    override fun check(item: CarbonReportBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBundleAllowedTrailerIdRule(var allowed: MutableList<String>) : CarbonReportBundleRule {
    override fun check(item: CarbonReportBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBundleRequiredTitleRule(var message: String) : CarbonReportBundleRule {
    override fun check(item: CarbonReportBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBundleRequiredSourceRule(var message: String) : CarbonReportBundleRule {
    override fun check(item: CarbonReportBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report bundle. */
data class CarbonReportBundleValidator(var rules: MutableList<CarbonReportBundleRule>) {
    fun validate(item: CarbonReportBundle): MutableList<String> {
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

    fun requireValid(item: CarbonReportBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportBundleValidator(): CarbonReportBundleValidator {
    return CarbonReportBundleValidator(
        mutableListOf<CarbonReportBundleRule>(
            CarbonReportBundleRequiredChannelRule("carbon report bundle needs an entry"),
            CarbonReportBundleRequiredCodeRule("carbon report bundle needs a value"),
            CarbonReportBundleAllowedTrailerIdRule(mutableListOf<String>("legacy")),
            CarbonReportBundleRequiredTitleRule("carbon report bundle needs this field"),
            CarbonReportBundleRequiredSourceRule("carbon report bundle needs a value"),
        ),
    )
}
