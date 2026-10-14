package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize

/** A rule a strict dispute plan must pass. Check returns its problems, or none. */
interface StrictDisputePlanRule {
    fun check(item: StrictDisputePlan): MutableList<String>
}

data class StrictDisputePlanRequiredLaneRule(var message: String) : StrictDisputePlanRule {
    override fun check(item: StrictDisputePlan): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputePlanGraceDaysRangeRule(var min: Int, var max: Int) : StrictDisputePlanRule {
    override fun check(item: StrictDisputePlan): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputePlanStartsAtRangeRule(var min: Int, var max: Int) : StrictDisputePlanRule {
    override fun check(item: StrictDisputePlan): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputePlanRequiredCodeRule(var message: String) : StrictDisputePlanRule {
    override fun check(item: StrictDisputePlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputePlanAllowedContactEmailRule(var allowed: MutableList<String>) : StrictDisputePlanRule {
    override fun check(item: StrictDisputePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dispute plan. */
data class StrictDisputePlanValidator(var rules: MutableList<StrictDisputePlanRule>) {
    fun validate(item: StrictDisputePlan): MutableList<String> {
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

    fun requireValid(item: StrictDisputePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDisputePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDisputePlanValidator(): StrictDisputePlanValidator {
    return StrictDisputePlanValidator(
        mutableListOf<StrictDisputePlanRule>(
            StrictDisputePlanRequiredLaneRule("strict dispute plan needs this field"),
            StrictDisputePlanGraceDaysRangeRule(255, 86400),
            StrictDisputePlanStartsAtRangeRule(255, 255),
            StrictDisputePlanRequiredCodeRule("strict dispute plan needs an entry"),
            StrictDisputePlanAllowedContactEmailRule(mutableListOf<String>("lenient", "global", "rolling")),
        ),
    )
}
