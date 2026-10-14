package com.acme.transport.emissions.routeoffset.offsetcheckpoint

/** A rule a scheduled route offset summary must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetSummaryRule {
    fun check(item: ScheduledRouteOffsetSummary): MutableList<String>
}

data class ScheduledRouteOffsetSummaryRequiredCodeRule(var message: String) : ScheduledRouteOffsetSummaryRule {
    override fun check(item: ScheduledRouteOffsetSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSummaryCodeLengthRule(var maxLength: Int) : ScheduledRouteOffsetSummaryRule {
    override fun check(item: ScheduledRouteOffsetSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSummaryRequiredShiftCodeRule(
    var message: String,
) : ScheduledRouteOffsetSummaryRule {
    override fun check(item: ScheduledRouteOffsetSummary): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSummaryRequiredNameRule(var message: String) : ScheduledRouteOffsetSummaryRule {
    override fun check(item: ScheduledRouteOffsetSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled route offset summary. */
data class ScheduledRouteOffsetSummaryValidator(var rules: MutableList<ScheduledRouteOffsetSummaryRule>) {
    fun validate(item: ScheduledRouteOffsetSummary): MutableList<String> {
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

    fun requireValid(item: ScheduledRouteOffsetSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetSummaryValidator(): ScheduledRouteOffsetSummaryValidator {
    return ScheduledRouteOffsetSummaryValidator(
        mutableListOf<ScheduledRouteOffsetSummaryRule>(
            ScheduledRouteOffsetSummaryRequiredCodeRule("scheduled route offset summary needs an entry"),
            ScheduledRouteOffsetSummaryCodeLengthRule(16),
            ScheduledRouteOffsetSummaryRequiredShiftCodeRule("scheduled route offset summary needs an entry"),
            ScheduledRouteOffsetSummaryRequiredNameRule("scheduled route offset summary needs a value"),
        ),
    )
}
