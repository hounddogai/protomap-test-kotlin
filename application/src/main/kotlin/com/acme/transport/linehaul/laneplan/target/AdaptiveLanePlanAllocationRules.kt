package com.acme.transport.linehaul.laneplan.target

/** A rule a adaptive lane plan allocation must pass. Check returns its problems, or none. */
interface AdaptiveLanePlanAllocationRule {
    fun check(item: AdaptiveLanePlanAllocation): MutableList<String>
}

data class AdaptiveLanePlanAllocationRequiredCodeRule(var message: String) : AdaptiveLanePlanAllocationRule {
    override fun check(item: AdaptiveLanePlanAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanAllocationRequiredTitleRule(var message: String) : AdaptiveLanePlanAllocationRule {
    override fun check(item: AdaptiveLanePlanAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanAllocationCodeLengthRule(var maxLength: Int) : AdaptiveLanePlanAllocationRule {
    override fun check(item: AdaptiveLanePlanAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lane plan allocation. */
data class AdaptiveLanePlanAllocationValidator(var rules: MutableList<AdaptiveLanePlanAllocationRule>) {
    fun validate(item: AdaptiveLanePlanAllocation): MutableList<String> {
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

    fun requireValid(item: AdaptiveLanePlanAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLanePlanAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLanePlanAllocationValidator(): AdaptiveLanePlanAllocationValidator {
    return AdaptiveLanePlanAllocationValidator(
        mutableListOf<AdaptiveLanePlanAllocationRule>(
            AdaptiveLanePlanAllocationRequiredCodeRule("adaptive lane plan allocation needs a value"),
            AdaptiveLanePlanAllocationRequiredTitleRule("adaptive lane plan allocation needs a value"),
            AdaptiveLanePlanAllocationCodeLengthRule(32),
        ),
    )
}
