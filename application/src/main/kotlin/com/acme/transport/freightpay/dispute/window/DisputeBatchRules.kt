package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize

/** A rule a dispute batch must pass. Check returns its problems, or none. */
interface DisputeBatchRule {
    fun check(item: DisputeBatch): MutableList<String>
}

data class DisputeBatchRequiredLabelRule(var message: String) : DisputeBatchRule {
    override fun check(item: DisputeBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeBatchAllowedRouteCodeRule(var allowed: MutableList<String>) : DisputeBatchRule {
    override fun check(item: DisputeBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBatchRequiredTitleRule(var message: String) : DisputeBatchRule {
    override fun check(item: DisputeBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute batch. */
data class DisputeBatchValidator(var rules: MutableList<DisputeBatchRule>) {
    fun validate(item: DisputeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeBatchValidator(): DisputeBatchValidator {
    return DisputeBatchValidator(
        mutableListOf<DisputeBatchRule>(
            DisputeBatchRequiredLabelRule("dispute batch needs a value"),
            DisputeBatchAllowedRouteCodeRule(mutableListOf<String>("rolling")),
            DisputeBatchRequiredTitleRule("dispute batch needs this field"),
        ),
    )
}
