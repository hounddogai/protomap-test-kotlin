package com.acme.transport.network.node.allocation

/** A rule a lenient node allocation must pass. Check returns its problems, or none. */
interface LenientNodeAllocationRule {
    fun check(item: LenientNodeAllocation): MutableList<String>
}

data class LenientNodeAllocationLabelLengthRule(var maxLength: Int) : LenientNodeAllocationRule {
    override fun check(item: LenientNodeAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeAllocationRequiredLabelRule(var message: String) : LenientNodeAllocationRule {
    override fun check(item: LenientNodeAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeAllocationCodeLengthRule(var maxLength: Int) : LenientNodeAllocationRule {
    override fun check(item: LenientNodeAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient node allocation. */
data class LenientNodeAllocationValidator(var rules: MutableList<LenientNodeAllocationRule>) {
    fun validate(item: LenientNodeAllocation): MutableList<String> {
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

    fun requireValid(item: LenientNodeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNodeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNodeAllocationValidator(): LenientNodeAllocationValidator {
    return LenientNodeAllocationValidator(
        mutableListOf<LenientNodeAllocationRule>(
            LenientNodeAllocationLabelLengthRule(64),
            LenientNodeAllocationRequiredLabelRule("lenient node allocation needs this field"),
            LenientNodeAllocationCodeLengthRule(86400),
        ),
    )
}
