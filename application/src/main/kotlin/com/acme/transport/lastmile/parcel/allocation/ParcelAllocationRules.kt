package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize

/** A rule a parcel allocation must pass. Check returns its problems, or none. */
interface ParcelAllocationRule {
    fun check(item: ParcelAllocation): MutableList<String>
}

data class ParcelAllocationCodeLengthRule(var maxLength: Int) : ParcelAllocationRule {
    override fun check(item: ParcelAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAllocationAllowedCodeRule(var allowed: MutableList<String>) : ParcelAllocationRule {
    override fun check(item: ParcelAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAllocationNameLengthRule(var maxLength: Int) : ParcelAllocationRule {
    override fun check(item: ParcelAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel allocation. */
data class ParcelAllocationValidator(var rules: MutableList<ParcelAllocationRule>) {
    fun validate(item: ParcelAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelAllocationValidator(): ParcelAllocationValidator {
    return ParcelAllocationValidator(
        mutableListOf<ParcelAllocationRule>(
            ParcelAllocationCodeLengthRule(1000),
            ParcelAllocationAllowedCodeRule(mutableListOf<String>("fallback", "rolling")),
            ParcelAllocationNameLengthRule(86400),
        ),
    )
}
