package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.text.normalize

/** A rule a lenient idling allocation must pass. Check returns its problems, or none. */
interface LenientIdlingAllocationRule {
    fun check(item: LenientIdlingAllocation): MutableList<String>
}

data class LenientIdlingAllocationRequiredCodeRule(var message: String) : LenientIdlingAllocationRule {
    override fun check(item: LenientIdlingAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingAllocationReviewerLengthRule(var maxLength: Int) : LenientIdlingAllocationRule {
    override fun check(item: LenientIdlingAllocation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingAllocationCodeLengthRule(var maxLength: Int) : LenientIdlingAllocationRule {
    override fun check(item: LenientIdlingAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingAllocationAllowedRegionRule(
    var allowed: MutableList<String>,
) : LenientIdlingAllocationRule {
    override fun check(item: LenientIdlingAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient idling allocation. */
data class LenientIdlingAllocationValidator(var rules: MutableList<LenientIdlingAllocationRule>) {
    fun validate(item: LenientIdlingAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientIdlingAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIdlingAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIdlingAllocationValidator(): LenientIdlingAllocationValidator {
    return LenientIdlingAllocationValidator(
        mutableListOf<LenientIdlingAllocationRule>(
            LenientIdlingAllocationRequiredCodeRule("lenient idling allocation needs this field"),
            LenientIdlingAllocationReviewerLengthRule(120),
            LenientIdlingAllocationCodeLengthRule(64),
            LenientIdlingAllocationAllowedRegionRule(mutableListOf<String>("capped", "default", "tiered")),
        ),
    )
}
