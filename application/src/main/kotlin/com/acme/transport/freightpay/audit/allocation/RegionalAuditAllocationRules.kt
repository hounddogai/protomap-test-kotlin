package com.acme.transport.freightpay.audit.allocation

/** A rule a regional audit allocation must pass. Check returns its problems, or none. */
interface RegionalAuditAllocationRule {
    fun check(item: RegionalAuditAllocation): MutableList<String>
}

data class RegionalAuditAllocationCodeLengthRule(var maxLength: Int) : RegionalAuditAllocationRule {
    override fun check(item: RegionalAuditAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditAllocationTitleLengthRule(var maxLength: Int) : RegionalAuditAllocationRule {
    override fun check(item: RegionalAuditAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditAllocationTrailerIdLengthRule(var maxLength: Int) : RegionalAuditAllocationRule {
    override fun check(item: RegionalAuditAllocation): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional audit allocation. */
data class RegionalAuditAllocationValidator(var rules: MutableList<RegionalAuditAllocationRule>) {
    fun validate(item: RegionalAuditAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAuditAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAuditAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAuditAllocationValidator(): RegionalAuditAllocationValidator {
    return RegionalAuditAllocationValidator(
        mutableListOf<RegionalAuditAllocationRule>(
            RegionalAuditAllocationCodeLengthRule(80),
            RegionalAuditAllocationTitleLengthRule(80),
            RegionalAuditAllocationTrailerIdLengthRule(3600),
        ),
    )
}
