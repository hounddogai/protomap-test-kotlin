package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize

/** A rule a hub allocation must pass. Check returns its problems, or none. */
interface HubAllocationRule {
    fun check(item: HubAllocation): MutableList<String>
}

data class HubAllocationPriorityRangeRule(var min: Int, var max: Int) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class HubAllocationQuantityRangeRule(var min: Int, var max: Int) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubAllocationRequiredOwnerIdRule(var message: String) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubAllocationAllowedCodeRule(var allowed: MutableList<String>) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class HubAllocationRequiredNameRule(var message: String) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubAllocationOwnerIdLengthRule(var maxLength: Int) : HubAllocationRule {
    override fun check(item: HubAllocation): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub allocation. */
data class HubAllocationValidator(var rules: MutableList<HubAllocationRule>) {
    fun validate(item: HubAllocation): MutableList<String> {
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

    fun requireValid(item: HubAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultHubAllocationValidator(): HubAllocationValidator {
    return HubAllocationValidator(
        mutableListOf<HubAllocationRule>(
            HubAllocationPriorityRangeRule(80, 3600),
            HubAllocationQuantityRangeRule(255, 1000),
            HubAllocationRequiredOwnerIdRule("hub allocation needs a value"),
            HubAllocationAllowedCodeRule(mutableListOf<String>("rolling", "lenient")),
            HubAllocationRequiredNameRule("hub allocation needs an entry"),
            HubAllocationOwnerIdLengthRule(64),
        ),
    )
}
