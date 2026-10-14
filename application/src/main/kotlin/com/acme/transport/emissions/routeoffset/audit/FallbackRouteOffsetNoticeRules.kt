package com.acme.transport.emissions.routeoffset.audit

/** A rule a fallback route offset notice must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetNoticeRule {
    fun check(item: FallbackRouteOffsetNotice): MutableList<String>
}

data class FallbackRouteOffsetNoticeCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetNoticeRule {
    override fun check(item: FallbackRouteOffsetNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetNoticeCodePrefixRule(var prefix: String) : FallbackRouteOffsetNoticeRule {
    override fun check(item: FallbackRouteOffsetNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetNoticeApprovedGuardRule(var limit: Double) : FallbackRouteOffsetNoticeRule {
    override fun check(item: FallbackRouteOffsetNotice): MutableList<String> {
        if (item.approved && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset notice. */
data class FallbackRouteOffsetNoticeValidator(var rules: MutableList<FallbackRouteOffsetNoticeRule>) {
    fun validate(item: FallbackRouteOffsetNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetNoticeValidator(): FallbackRouteOffsetNoticeValidator {
    return FallbackRouteOffsetNoticeValidator(
        mutableListOf<FallbackRouteOffsetNoticeRule>(
            FallbackRouteOffsetNoticeCodeLengthRule(16),
            FallbackRouteOffsetNoticeCodePrefixRule("Q"),
            FallbackRouteOffsetNoticeApprovedGuardRule(7.8),
        ),
    )
}
