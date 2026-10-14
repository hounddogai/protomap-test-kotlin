package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.normalize

/** A rule a pending route offset profile must pass. Check returns its problems, or none. */
interface PendingRouteOffsetProfileRule {
    fun check(item: PendingRouteOffsetProfile): MutableList<String>
}

data class PendingRouteOffsetProfileTitleLengthRule(var maxLength: Int) : PendingRouteOffsetProfileRule {
    override fun check(item: PendingRouteOffsetProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetProfileRequiredOwnerIdRule(var message: String) : PendingRouteOffsetProfileRule {
    override fun check(item: PendingRouteOffsetProfile): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetProfileAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : PendingRouteOffsetProfileRule {
    override fun check(item: PendingRouteOffsetProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetProfileRequiredCodeRule(var message: String) : PendingRouteOffsetProfileRule {
    override fun check(item: PendingRouteOffsetProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRouteOffsetProfileRequiredTitleRule(var message: String) : PendingRouteOffsetProfileRule {
    override fun check(item: PendingRouteOffsetProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending route offset profile. */
data class PendingRouteOffsetProfileValidator(var rules: MutableList<PendingRouteOffsetProfileRule>) {
    fun validate(item: PendingRouteOffsetProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRouteOffsetProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRouteOffsetProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRouteOffsetProfileValidator(): PendingRouteOffsetProfileValidator {
    return PendingRouteOffsetProfileValidator(
        mutableListOf<PendingRouteOffsetProfileRule>(
            PendingRouteOffsetProfileTitleLengthRule(3600),
            PendingRouteOffsetProfileRequiredOwnerIdRule("pending route offset profile needs this field"),
            PendingRouteOffsetProfileAllowedOwnerIdRule(mutableListOf<String>("legacy", "primary")),
            PendingRouteOffsetProfileRequiredCodeRule("pending route offset profile needs this field"),
            PendingRouteOffsetProfileRequiredTitleRule("pending route offset profile needs an entry"),
        ),
    )
}
