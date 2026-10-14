package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

/** A rule a global route offset quota must pass. Check returns its problems, or none. */
interface GlobalRouteOffsetQuotaRule {
    fun check(item: GlobalRouteOffsetQuota): MutableList<String>
}

data class GlobalRouteOffsetQuotaRequiredLaneRule(var message: String) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetQuotaLaneLengthRule(var maxLength: Int) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetQuotaRequiredCodeRule(var message: String) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetQuotaEndsAtRangeRule(var min: Int, var max: Int) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class GlobalRouteOffsetQuotaRequiredLabelRule(var message: String) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetQuotaThresholdRangeRule(
    var min: Double,
    var max: Double,
) : GlobalRouteOffsetQuotaRule {
    override fun check(item: GlobalRouteOffsetQuota): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global route offset quota. */
data class GlobalRouteOffsetQuotaValidator(var rules: MutableList<GlobalRouteOffsetQuotaRule>) {
    fun validate(item: GlobalRouteOffsetQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRouteOffsetQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRouteOffsetQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRouteOffsetQuotaValidator(): GlobalRouteOffsetQuotaValidator {
    return GlobalRouteOffsetQuotaValidator(
        mutableListOf<GlobalRouteOffsetQuotaRule>(
            GlobalRouteOffsetQuotaRequiredLaneRule("global route offset quota needs this field"),
            GlobalRouteOffsetQuotaLaneLengthRule(16),
            GlobalRouteOffsetQuotaRequiredCodeRule("global route offset quota needs a value"),
            GlobalRouteOffsetQuotaEndsAtRangeRule(64, 64),
            GlobalRouteOffsetQuotaRequiredLabelRule("global route offset quota needs an entry"),
            GlobalRouteOffsetQuotaThresholdRangeRule(8.7, 3354.6),
        ),
    )
}
