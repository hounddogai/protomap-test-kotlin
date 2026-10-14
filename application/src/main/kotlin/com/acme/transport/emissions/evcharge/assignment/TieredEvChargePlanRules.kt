package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.normalize

/** A rule a tiered ev charge plan must pass. Check returns its problems, or none. */
interface TieredEvChargePlanRule {
    fun check(item: TieredEvChargePlan): MutableList<String>
}

data class TieredEvChargePlanAllowedOwnerIdRule(var allowed: MutableList<String>) : TieredEvChargePlanRule {
    override fun check(item: TieredEvChargePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargePlanRequiredSummaryRule(var message: String) : TieredEvChargePlanRule {
    override fun check(item: TieredEvChargePlan): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargePlanTitleLengthRule(var maxLength: Int) : TieredEvChargePlanRule {
    override fun check(item: TieredEvChargePlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargePlanRequiredOwnerIdRule(var message: String) : TieredEvChargePlanRule {
    override fun check(item: TieredEvChargePlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargePlanArchivedGuardRule(var limit: Double) : TieredEvChargePlanRule {
    override fun check(item: TieredEvChargePlan): MutableList<String> {
        if (item.archived && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered ev charge plan. */
data class TieredEvChargePlanValidator(var rules: MutableList<TieredEvChargePlanRule>) {
    fun validate(item: TieredEvChargePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEvChargePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargePlanValidator(): TieredEvChargePlanValidator {
    return TieredEvChargePlanValidator(
        mutableListOf<TieredEvChargePlanRule>(
            TieredEvChargePlanAllowedOwnerIdRule(mutableListOf<String>("scheduled")),
            TieredEvChargePlanRequiredSummaryRule("tiered ev charge plan needs a value"),
            TieredEvChargePlanTitleLengthRule(120),
            TieredEvChargePlanRequiredOwnerIdRule("tiered ev charge plan needs an entry"),
            TieredEvChargePlanArchivedGuardRule(1.4),
        ),
    )
}
