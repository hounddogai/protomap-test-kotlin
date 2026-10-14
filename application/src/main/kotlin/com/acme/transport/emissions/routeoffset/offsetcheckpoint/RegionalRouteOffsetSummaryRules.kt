package com.acme.transport.emissions.routeoffset.offsetcheckpoint

/** A rule a regional route offset summary must pass. Check returns its problems, or none. */
interface RegionalRouteOffsetSummaryRule {
    fun check(item: RegionalRouteOffsetSummary): MutableList<String>
}

data class RegionalRouteOffsetSummaryLabelLengthRule(var maxLength: Int) : RegionalRouteOffsetSummaryRule {
    override fun check(item: RegionalRouteOffsetSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetSummarySourceLengthRule(var maxLength: Int) : RegionalRouteOffsetSummaryRule {
    override fun check(item: RegionalRouteOffsetSummary): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetSummaryCodePrefixRule(var prefix: String) : RegionalRouteOffsetSummaryRule {
    override fun check(item: RegionalRouteOffsetSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetSummaryRequiredLabelRule(var message: String) : RegionalRouteOffsetSummaryRule {
    override fun check(item: RegionalRouteOffsetSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRouteOffsetSummaryCodeLengthRule(var maxLength: Int) : RegionalRouteOffsetSummaryRule {
    override fun check(item: RegionalRouteOffsetSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional route offset summary. */
data class RegionalRouteOffsetSummaryValidator(var rules: MutableList<RegionalRouteOffsetSummaryRule>) {
    fun validate(item: RegionalRouteOffsetSummary): MutableList<String> {
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

    fun requireValid(item: RegionalRouteOffsetSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRouteOffsetSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRouteOffsetSummaryValidator(): RegionalRouteOffsetSummaryValidator {
    return RegionalRouteOffsetSummaryValidator(
        mutableListOf<RegionalRouteOffsetSummaryRule>(
            RegionalRouteOffsetSummaryLabelLengthRule(1000),
            RegionalRouteOffsetSummarySourceLengthRule(32),
            RegionalRouteOffsetSummaryCodePrefixRule("ACM"),
            RegionalRouteOffsetSummaryRequiredLabelRule("regional route offset summary needs a value"),
            RegionalRouteOffsetSummaryCodeLengthRule(120),
        ),
    )
}
