package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize

/** A rule a global availability allocation must pass. Check returns its problems, or none. */
interface GlobalAvailabilityAllocationRule {
    fun check(item: GlobalAvailabilityAllocation): MutableList<String>
}

data class GlobalAvailabilityAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalAvailabilityAllocationRule {
    override fun check(item: GlobalAvailabilityAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityAllocationNameLengthRule(var maxLength: Int) : GlobalAvailabilityAllocationRule {
    override fun check(item: GlobalAvailabilityAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityAllocationOwnerIdLengthRule(
    var maxLength: Int,
) : GlobalAvailabilityAllocationRule {
    override fun check(item: GlobalAvailabilityAllocation): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityAllocationRequiredNameRule(
    var message: String,
) : GlobalAvailabilityAllocationRule {
    override fun check(item: GlobalAvailabilityAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global availability allocation. */
data class GlobalAvailabilityAllocationValidator(var rules: MutableList<GlobalAvailabilityAllocationRule>) {
    fun validate(item: GlobalAvailabilityAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAvailabilityAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAvailabilityAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAvailabilityAllocationValidator(): GlobalAvailabilityAllocationValidator {
    return GlobalAvailabilityAllocationValidator(
        mutableListOf<GlobalAvailabilityAllocationRule>(
            GlobalAvailabilityAllocationAllowedCodeRule(mutableListOf<String>()),
            GlobalAvailabilityAllocationNameLengthRule(86400),
            GlobalAvailabilityAllocationOwnerIdLengthRule(16),
            GlobalAvailabilityAllocationRequiredNameRule("global availability allocation needs a value"),
        ),
    )
}
