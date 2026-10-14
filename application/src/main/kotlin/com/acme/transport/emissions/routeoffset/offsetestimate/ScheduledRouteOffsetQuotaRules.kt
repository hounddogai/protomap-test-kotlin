package com.acme.transport.emissions.routeoffset.offsetestimate

/** A rule a scheduled route offset quota must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetQuotaRule {
    fun check(item: ScheduledRouteOffsetQuota): MutableList<String>
}

data class ScheduledRouteOffsetQuotaRequiredShiftCodeRule(var message: String) : ScheduledRouteOffsetQuotaRule {
    override fun check(item: ScheduledRouteOffsetQuota): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetQuotaShiftCodeLengthRule(var maxLength: Int) : ScheduledRouteOffsetQuotaRule {
    override fun check(item: ScheduledRouteOffsetQuota): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetQuotaRequiredCodeRule(var message: String) : ScheduledRouteOffsetQuotaRule {
    override fun check(item: ScheduledRouteOffsetQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled route offset quota. */
data class ScheduledRouteOffsetQuotaValidator(var rules: MutableList<ScheduledRouteOffsetQuotaRule>) {
    fun validate(item: ScheduledRouteOffsetQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRouteOffsetQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetQuotaValidator(): ScheduledRouteOffsetQuotaValidator {
    return ScheduledRouteOffsetQuotaValidator(
        mutableListOf<ScheduledRouteOffsetQuotaRule>(
            ScheduledRouteOffsetQuotaRequiredShiftCodeRule("scheduled route offset quota needs this field"),
            ScheduledRouteOffsetQuotaShiftCodeLengthRule(3600),
            ScheduledRouteOffsetQuotaRequiredCodeRule("scheduled route offset quota needs a value"),
        ),
    )
}
