package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize

/** A rule a scheduled cross dock claim must pass. Check returns its problems, or none. */
interface ScheduledCrossDockClaimRule {
    fun check(item: ScheduledCrossDockClaim): MutableList<String>
}

data class ScheduledCrossDockClaimAllowedSegmentRule(
    var allowed: MutableList<String>,
) : ScheduledCrossDockClaimRule {
    override fun check(item: ScheduledCrossDockClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockClaimSegmentLengthRule(var maxLength: Int) : ScheduledCrossDockClaimRule {
    override fun check(item: ScheduledCrossDockClaim): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockClaimRequiredNameRule(var message: String) : ScheduledCrossDockClaimRule {
    override fun check(item: ScheduledCrossDockClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled cross dock claim. */
data class ScheduledCrossDockClaimValidator(var rules: MutableList<ScheduledCrossDockClaimRule>) {
    fun validate(item: ScheduledCrossDockClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCrossDockClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCrossDockClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCrossDockClaimValidator(): ScheduledCrossDockClaimValidator {
    return ScheduledCrossDockClaimValidator(
        mutableListOf<ScheduledCrossDockClaimRule>(
            ScheduledCrossDockClaimAllowedSegmentRule(mutableListOf<String>("adaptive", "scheduled", "legacy")),
            ScheduledCrossDockClaimSegmentLengthRule(255),
            ScheduledCrossDockClaimRequiredNameRule("scheduled cross dock claim needs a value"),
        ),
    )
}
