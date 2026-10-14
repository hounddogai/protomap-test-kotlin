package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize

/** A rule a adaptive node allocation must pass. Check returns its problems, or none. */
interface AdaptiveNodeAllocationRule {
    fun check(item: AdaptiveNodeAllocation): MutableList<String>
}

data class AdaptiveNodeAllocationRequiredCategoryRule(var message: String) : AdaptiveNodeAllocationRule {
    override fun check(item: AdaptiveNodeAllocation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeAllocationRequiredCodeRule(var message: String) : AdaptiveNodeAllocationRule {
    override fun check(item: AdaptiveNodeAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeAllocationRequiredNameRule(var message: String) : AdaptiveNodeAllocationRule {
    override fun check(item: AdaptiveNodeAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveNodeAllocationRule {
    override fun check(item: AdaptiveNodeAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeAllocationCodeLengthRule(var maxLength: Int) : AdaptiveNodeAllocationRule {
    override fun check(item: AdaptiveNodeAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node allocation. */
data class AdaptiveNodeAllocationValidator(var rules: MutableList<AdaptiveNodeAllocationRule>) {
    fun validate(item: AdaptiveNodeAllocation): MutableList<String> {
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

    fun requireValid(item: AdaptiveNodeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeAllocationValidator(): AdaptiveNodeAllocationValidator {
    return AdaptiveNodeAllocationValidator(
        mutableListOf<AdaptiveNodeAllocationRule>(
            AdaptiveNodeAllocationRequiredCategoryRule("adaptive node allocation needs a value"),
            AdaptiveNodeAllocationRequiredCodeRule("adaptive node allocation needs this field"),
            AdaptiveNodeAllocationRequiredNameRule("adaptive node allocation needs a value"),
            AdaptiveNodeAllocationAllowedCodeRule(mutableListOf<String>("primary")),
            AdaptiveNodeAllocationCodeLengthRule(16),
        ),
    )
}
