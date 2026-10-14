package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.normalize

/** A rule a dock door claim must pass. Check returns its problems, or none. */
interface DockDoorClaimRule {
    fun check(item: DockDoorClaim): MutableList<String>
}

data class DockDoorClaimRequiredTitleRule(var message: String) : DockDoorClaimRule {
    override fun check(item: DockDoorClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorClaimRequiredCodeRule(var message: String) : DockDoorClaimRule {
    override fun check(item: DockDoorClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorClaimAllowedSummaryRule(var allowed: MutableList<String>) : DockDoorClaimRule {
    override fun check(item: DockDoorClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door claim. */
data class DockDoorClaimValidator(var rules: MutableList<DockDoorClaimRule>) {
    fun validate(item: DockDoorClaim): MutableList<String> {
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

    fun requireValid(item: DockDoorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorClaimValidator(): DockDoorClaimValidator {
    return DockDoorClaimValidator(
        mutableListOf<DockDoorClaimRule>(
            DockDoorClaimRequiredTitleRule("dock door claim needs this field"),
            DockDoorClaimRequiredCodeRule("dock door claim needs an entry"),
            DockDoorClaimAllowedSummaryRule(mutableListOf<String>("pending")),
        ),
    )
}
