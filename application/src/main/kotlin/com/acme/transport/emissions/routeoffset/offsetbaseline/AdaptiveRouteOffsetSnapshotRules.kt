package com.acme.transport.emissions.routeoffset.offsetbaseline

/** A rule a adaptive route offset snapshot must pass. Check returns its problems, or none. */
interface AdaptiveRouteOffsetSnapshotRule {
    fun check(item: AdaptiveRouteOffsetSnapshot): MutableList<String>
}

data class AdaptiveRouteOffsetSnapshotNameLengthRule(var maxLength: Int) : AdaptiveRouteOffsetSnapshotRule {
    override fun check(item: AdaptiveRouteOffsetSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetSnapshotCodeLengthRule(var maxLength: Int) : AdaptiveRouteOffsetSnapshotRule {
    override fun check(item: AdaptiveRouteOffsetSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetSnapshotCodePrefixRule(var prefix: String) : AdaptiveRouteOffsetSnapshotRule {
    override fun check(item: AdaptiveRouteOffsetSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive route offset snapshot. */
data class AdaptiveRouteOffsetSnapshotValidator(var rules: MutableList<AdaptiveRouteOffsetSnapshotRule>) {
    fun validate(item: AdaptiveRouteOffsetSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRouteOffsetSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRouteOffsetSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRouteOffsetSnapshotValidator(): AdaptiveRouteOffsetSnapshotValidator {
    return AdaptiveRouteOffsetSnapshotValidator(
        mutableListOf<AdaptiveRouteOffsetSnapshotRule>(
            AdaptiveRouteOffsetSnapshotNameLengthRule(32),
            AdaptiveRouteOffsetSnapshotCodeLengthRule(16),
            AdaptiveRouteOffsetSnapshotCodePrefixRule("X"),
        ),
    )
}
