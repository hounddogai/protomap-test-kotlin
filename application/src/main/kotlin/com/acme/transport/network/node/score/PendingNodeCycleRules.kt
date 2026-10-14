package com.acme.transport.network.node.score

import com.acme.transport.kit.text.normalize

/** A rule a pending node cycle must pass. Check returns its problems, or none. */
interface PendingNodeCycleRule {
    fun check(item: PendingNodeCycle): MutableList<String>
}

data class PendingNodeCycleTitleLengthRule(var maxLength: Int) : PendingNodeCycleRule {
    override fun check(item: PendingNodeCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeCycleContactEmailLengthRule(var maxLength: Int) : PendingNodeCycleRule {
    override fun check(item: PendingNodeCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeCycleAllowedSummaryRule(var allowed: MutableList<String>) : PendingNodeCycleRule {
    override fun check(item: PendingNodeCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending node cycle. */
data class PendingNodeCycleValidator(var rules: MutableList<PendingNodeCycleRule>) {
    fun validate(item: PendingNodeCycle): MutableList<String> {
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

    fun requireValid(item: PendingNodeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodeCycleValidator(): PendingNodeCycleValidator {
    return PendingNodeCycleValidator(
        mutableListOf<PendingNodeCycleRule>(
            PendingNodeCycleTitleLengthRule(255),
            PendingNodeCycleContactEmailLengthRule(80),
            PendingNodeCycleAllowedSummaryRule(mutableListOf<String>("primary", "rolling")),
        ),
    )
}
