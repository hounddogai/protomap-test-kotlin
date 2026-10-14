package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.text.normalize

/** A rule a cutoff summary must pass. Check returns its problems, or none. */
interface CutoffSummaryRule {
    fun check(item: CutoffSummary): MutableList<String>
}

data class CutoffSummaryRequiredCodeRule(var message: String) : CutoffSummaryRule {
    override fun check(item: CutoffSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffSummaryRequiredLabelRule(var message: String) : CutoffSummaryRule {
    override fun check(item: CutoffSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffSummaryRequiredRouteCodeRule(var message: String) : CutoffSummaryRule {
    override fun check(item: CutoffSummary): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffSummaryAllowedCodeRule(var allowed: MutableList<String>) : CutoffSummaryRule {
    override fun check(item: CutoffSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CutoffSummaryRouteCodeLengthRule(var maxLength: Int) : CutoffSummaryRule {
    override fun check(item: CutoffSummary): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff summary. */
data class CutoffSummaryValidator(var rules: MutableList<CutoffSummaryRule>) {
    fun validate(item: CutoffSummary): MutableList<String> {
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

    fun requireValid(item: CutoffSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffSummaryValidator(): CutoffSummaryValidator {
    return CutoffSummaryValidator(
        mutableListOf<CutoffSummaryRule>(
            CutoffSummaryRequiredCodeRule("cutoff summary needs a value"),
            CutoffSummaryRequiredLabelRule("cutoff summary needs an entry"),
            CutoffSummaryRequiredRouteCodeRule("cutoff summary needs this field"),
            CutoffSummaryAllowedCodeRule(mutableListOf<String>("lenient")),
            CutoffSummaryRouteCodeLengthRule(32),
        ),
    )
}
