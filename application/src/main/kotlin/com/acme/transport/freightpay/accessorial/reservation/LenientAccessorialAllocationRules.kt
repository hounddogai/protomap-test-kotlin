package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize

/** A rule a lenient accessorial allocation must pass. Check returns its problems, or none. */
interface LenientAccessorialAllocationRule {
    fun check(item: LenientAccessorialAllocation): MutableList<String>
}

data class LenientAccessorialAllocationRequiredTimezoneRule(
    var message: String,
) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialAllocationTimezoneLengthRule(
    var maxLength: Int,
) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialAllocationRequiredSourceRule(
    var message: String,
) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialAllocationRequiredNameRule(
    var message: String,
) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialAllocationCodePrefixRule(var prefix: String) : LenientAccessorialAllocationRule {
    override fun check(item: LenientAccessorialAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient accessorial allocation. */
data class LenientAccessorialAllocationValidator(var rules: MutableList<LenientAccessorialAllocationRule>) {
    fun validate(item: LenientAccessorialAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAccessorialAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAccessorialAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAccessorialAllocationValidator(): LenientAccessorialAllocationValidator {
    return LenientAccessorialAllocationValidator(
        mutableListOf<LenientAccessorialAllocationRule>(
            LenientAccessorialAllocationRequiredTimezoneRule("lenient accessorial allocation needs a value"),
            LenientAccessorialAllocationTimezoneLengthRule(120),
            LenientAccessorialAllocationRequiredSourceRule("lenient accessorial allocation needs this field"),
            LenientAccessorialAllocationAllowedCodeRule(mutableListOf<String>()),
            LenientAccessorialAllocationRequiredNameRule("lenient accessorial allocation needs a value"),
            LenientAccessorialAllocationCodePrefixRule("REF"),
        ),
    )
}
