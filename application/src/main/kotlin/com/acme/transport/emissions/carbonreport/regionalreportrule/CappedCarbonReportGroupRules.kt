package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize

/** A rule a capped carbon report group must pass. Check returns its problems, or none. */
interface CappedCarbonReportGroupRule {
    fun check(item: CappedCarbonReportGroup): MutableList<String>
}

data class CappedCarbonReportGroupCodePrefixRule(var prefix: String) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportGroupRequiredCodeRule(var message: String) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportGroupRequiredNameRule(var message: String) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportGroupRequiredTitleRule(var message: String) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportGroupCodeLengthRule(var maxLength: Int) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportGroupAllowedNameRule(
    var allowed: MutableList<String>,
) : CappedCarbonReportGroupRule {
    override fun check(item: CappedCarbonReportGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped carbon report group. */
data class CappedCarbonReportGroupValidator(var rules: MutableList<CappedCarbonReportGroupRule>) {
    fun validate(item: CappedCarbonReportGroup): MutableList<String> {
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

    fun requireValid(item: CappedCarbonReportGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCarbonReportGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCarbonReportGroupValidator(): CappedCarbonReportGroupValidator {
    return CappedCarbonReportGroupValidator(
        mutableListOf<CappedCarbonReportGroupRule>(
            CappedCarbonReportGroupCodePrefixRule("REF"),
            CappedCarbonReportGroupRequiredCodeRule("capped carbon report group needs this field"),
            CappedCarbonReportGroupRequiredNameRule("capped carbon report group needs an entry"),
            CappedCarbonReportGroupRequiredTitleRule("capped carbon report group needs an entry"),
            CappedCarbonReportGroupCodeLengthRule(3600),
            CappedCarbonReportGroupAllowedNameRule(mutableListOf<String>("fallback", "tiered", "strict")),
        ),
    )
}
