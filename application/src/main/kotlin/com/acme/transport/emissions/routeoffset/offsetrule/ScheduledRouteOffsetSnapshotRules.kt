package com.acme.transport.emissions.routeoffset.offsetrule

/** A rule a scheduled route offset snapshot must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetSnapshotRule {
    fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String>
}

data class ScheduledRouteOffsetSnapshotCodePrefixRule(var prefix: String) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSnapshotActiveGuardRule(var limit: Int) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        if (item.active && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSnapshotRequiredLabelRule(
    var message: String,
) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSnapshotCodeLengthRule(var maxLength: Int) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSnapshotWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetSnapshotLabelLengthRule(var maxLength: Int) : ScheduledRouteOffsetSnapshotRule {
    override fun check(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled route offset snapshot. */
data class ScheduledRouteOffsetSnapshotValidator(var rules: MutableList<ScheduledRouteOffsetSnapshotRule>) {
    fun validate(item: ScheduledRouteOffsetSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRouteOffsetSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetSnapshotValidator(): ScheduledRouteOffsetSnapshotValidator {
    return ScheduledRouteOffsetSnapshotValidator(
        mutableListOf<ScheduledRouteOffsetSnapshotRule>(
            ScheduledRouteOffsetSnapshotCodePrefixRule("ACM"),
            ScheduledRouteOffsetSnapshotActiveGuardRule(64),
            ScheduledRouteOffsetSnapshotRequiredLabelRule("scheduled route offset snapshot needs an entry"),
            ScheduledRouteOffsetSnapshotCodeLengthRule(86400),
            ScheduledRouteOffsetSnapshotWindowDaysRangeRule(16, 86400),
            ScheduledRouteOffsetSnapshotLabelLengthRule(64),
        ),
    )
}
