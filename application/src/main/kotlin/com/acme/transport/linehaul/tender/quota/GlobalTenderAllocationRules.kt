package com.acme.transport.linehaul.tender.quota

/** A rule a global tender allocation must pass. Check returns its problems, or none. */
interface GlobalTenderAllocationRule {
    fun check(item: GlobalTenderAllocation): MutableList<String>
}

data class GlobalTenderAllocationCodePrefixRule(var prefix: String) : GlobalTenderAllocationRule {
    override fun check(item: GlobalTenderAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderAllocationTitleLengthRule(var maxLength: Int) : GlobalTenderAllocationRule {
    override fun check(item: GlobalTenderAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderAllocationDepotLengthRule(var maxLength: Int) : GlobalTenderAllocationRule {
    override fun check(item: GlobalTenderAllocation): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender allocation. */
data class GlobalTenderAllocationValidator(var rules: MutableList<GlobalTenderAllocationRule>) {
    fun validate(item: GlobalTenderAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderAllocationValidator(): GlobalTenderAllocationValidator {
    return GlobalTenderAllocationValidator(
        mutableListOf<GlobalTenderAllocationRule>(
            GlobalTenderAllocationCodePrefixRule("REF"),
            GlobalTenderAllocationTitleLengthRule(32),
            GlobalTenderAllocationDepotLengthRule(16),
        ),
    )
}
