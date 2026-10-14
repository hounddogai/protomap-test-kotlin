package com.acme.transport.fleetops.defect.cycle

/** A rule a regional defect allocation must pass. Check returns its problems, or none. */
interface RegionalDefectAllocationRule {
    fun check(item: RegionalDefectAllocation): MutableList<String>
}

data class RegionalDefectAllocationRequiredCarrierRule(var message: String) : RegionalDefectAllocationRule {
    override fun check(item: RegionalDefectAllocation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectAllocationRequiredReviewerRule(var message: String) : RegionalDefectAllocationRule {
    override fun check(item: RegionalDefectAllocation): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectAllocationReviewerLengthRule(var maxLength: Int) : RegionalDefectAllocationRule {
    override fun check(item: RegionalDefectAllocation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectAllocationCarrierLengthRule(var maxLength: Int) : RegionalDefectAllocationRule {
    override fun check(item: RegionalDefectAllocation): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectAllocationCodePrefixRule(var prefix: String) : RegionalDefectAllocationRule {
    override fun check(item: RegionalDefectAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect allocation. */
data class RegionalDefectAllocationValidator(var rules: MutableList<RegionalDefectAllocationRule>) {
    fun validate(item: RegionalDefectAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectAllocationValidator(): RegionalDefectAllocationValidator {
    return RegionalDefectAllocationValidator(
        mutableListOf<RegionalDefectAllocationRule>(
            RegionalDefectAllocationRequiredCarrierRule("regional defect allocation needs an entry"),
            RegionalDefectAllocationRequiredReviewerRule("regional defect allocation needs an entry"),
            RegionalDefectAllocationReviewerLengthRule(80),
            RegionalDefectAllocationCarrierLengthRule(64),
            RegionalDefectAllocationCodePrefixRule("Q"),
        ),
    )
}
