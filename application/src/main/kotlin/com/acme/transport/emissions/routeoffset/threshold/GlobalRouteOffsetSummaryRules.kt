package com.acme.transport.emissions.routeoffset.threshold

/** A rule a global route offset summary must pass. Check returns its problems, or none. */
interface GlobalRouteOffsetSummaryRule {
    fun check(item: GlobalRouteOffsetSummary): MutableList<String>
}

data class GlobalRouteOffsetSummaryLabelLengthRule(var maxLength: Int) : GlobalRouteOffsetSummaryRule {
    override fun check(item: GlobalRouteOffsetSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetSummaryRequiredLabelRule(var message: String) : GlobalRouteOffsetSummaryRule {
    override fun check(item: GlobalRouteOffsetSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetSummaryRequiredCodeRule(var message: String) : GlobalRouteOffsetSummaryRule {
    override fun check(item: GlobalRouteOffsetSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetSummaryCodeLengthRule(var maxLength: Int) : GlobalRouteOffsetSummaryRule {
    override fun check(item: GlobalRouteOffsetSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global route offset summary. */
data class GlobalRouteOffsetSummaryValidator(var rules: MutableList<GlobalRouteOffsetSummaryRule>) {
    fun validate(item: GlobalRouteOffsetSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRouteOffsetSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRouteOffsetSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRouteOffsetSummaryValidator(): GlobalRouteOffsetSummaryValidator {
    return GlobalRouteOffsetSummaryValidator(
        mutableListOf<GlobalRouteOffsetSummaryRule>(
            GlobalRouteOffsetSummaryLabelLengthRule(120),
            GlobalRouteOffsetSummaryRequiredLabelRule("global route offset summary needs a value"),
            GlobalRouteOffsetSummaryRequiredCodeRule("global route offset summary needs a value"),
            GlobalRouteOffsetSummaryCodeLengthRule(80),
        ),
    )
}
