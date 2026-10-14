package com.acme.transport.linehaul.tender.exception

/** A rule a strict tender allocation must pass. Check returns its problems, or none. */
interface StrictTenderAllocationRule {
    fun check(item: StrictTenderAllocation): MutableList<String>
}

data class StrictTenderAllocationCodeLengthRule(var maxLength: Int) : StrictTenderAllocationRule {
    override fun check(item: StrictTenderAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderAllocationRevisionRangeRule(var min: Int, var max: Int) : StrictTenderAllocationRule {
    override fun check(item: StrictTenderAllocation): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderAllocationNameLengthRule(var maxLength: Int) : StrictTenderAllocationRule {
    override fun check(item: StrictTenderAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tender allocation. */
data class StrictTenderAllocationValidator(var rules: MutableList<StrictTenderAllocationRule>) {
    fun validate(item: StrictTenderAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTenderAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTenderAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTenderAllocationValidator(): StrictTenderAllocationValidator {
    return StrictTenderAllocationValidator(
        mutableListOf<StrictTenderAllocationRule>(
            StrictTenderAllocationCodeLengthRule(1000),
            StrictTenderAllocationRevisionRangeRule(64, 64),
            StrictTenderAllocationNameLengthRule(80),
        ),
    )
}
