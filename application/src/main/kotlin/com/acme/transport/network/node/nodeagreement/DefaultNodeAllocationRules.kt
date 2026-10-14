package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize

/** A rule a default node allocation must pass. Check returns its problems, or none. */
interface DefaultNodeAllocationRule {
    fun check(item: DefaultNodeAllocation): MutableList<String>
}

data class DefaultNodeAllocationRequiredCodeRule(var message: String) : DefaultNodeAllocationRule {
    override fun check(item: DefaultNodeAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAllocationRequiredTitleRule(var message: String) : DefaultNodeAllocationRule {
    override fun check(item: DefaultNodeAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAllocationTitleLengthRule(var maxLength: Int) : DefaultNodeAllocationRule {
    override fun check(item: DefaultNodeAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAllocationAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : DefaultNodeAllocationRule {
    override fun check(item: DefaultNodeAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNodeAllocationDriverIdLengthRule(var maxLength: Int) : DefaultNodeAllocationRule {
    override fun check(item: DefaultNodeAllocation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default node allocation. */
data class DefaultNodeAllocationValidator(var rules: MutableList<DefaultNodeAllocationRule>) {
    fun validate(item: DefaultNodeAllocation): MutableList<String> {
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

    fun requireValid(item: DefaultNodeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNodeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNodeAllocationValidator(): DefaultNodeAllocationValidator {
    return DefaultNodeAllocationValidator(
        mutableListOf<DefaultNodeAllocationRule>(
            DefaultNodeAllocationRequiredCodeRule("default node allocation needs a value"),
            DefaultNodeAllocationRequiredTitleRule("default node allocation needs a value"),
            DefaultNodeAllocationTitleLengthRule(86400),
            DefaultNodeAllocationAllowedRouteCodeRule(mutableListOf<String>("legacy")),
            DefaultNodeAllocationDriverIdLengthRule(120),
        ),
    )
}
