package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.kit.text.normalize

/** A rule a rolling lane plan ledger must pass. Check returns its problems, or none. */
interface RollingLanePlanLedgerRule {
    fun check(item: RollingLanePlanLedger): MutableList<String>
}

data class RollingLanePlanLedgerTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingLanePlanLedgerRule {
    override fun check(item: RollingLanePlanLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RollingLanePlanLedgerRequiredCodeRule(var message: String) : RollingLanePlanLedgerRule {
    override fun check(item: RollingLanePlanLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanLedgerAllowedCodeRule(var allowed: MutableList<String>) : RollingLanePlanLedgerRule {
    override fun check(item: RollingLanePlanLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling lane plan ledger. */
data class RollingLanePlanLedgerValidator(var rules: MutableList<RollingLanePlanLedgerRule>) {
    fun validate(item: RollingLanePlanLedger): MutableList<String> {
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

    fun requireValid(item: RollingLanePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLanePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLanePlanLedgerValidator(): RollingLanePlanLedgerValidator {
    return RollingLanePlanLedgerValidator(
        mutableListOf<RollingLanePlanLedgerRule>(
            RollingLanePlanLedgerTagsLimitRule(16, mutableListOf<String>("weighted", "manual")),
            RollingLanePlanLedgerRequiredCodeRule("rolling lane plan ledger needs a value"),
            RollingLanePlanLedgerAllowedCodeRule(mutableListOf<String>("regional", "scheduled", "strict")),
        ),
    )
}
