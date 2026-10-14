package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize

/** A rule a lenient backhaul window must pass. Check returns its problems, or none. */
interface LenientBackhaulWindowRule {
    fun check(item: LenientBackhaulWindow): MutableList<String>
}

data class LenientBackhaulWindowSegmentLengthRule(var maxLength: Int) : LenientBackhaulWindowRule {
    override fun check(item: LenientBackhaulWindow): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulWindowAllowedCodeRule(var allowed: MutableList<String>) : LenientBackhaulWindowRule {
    override fun check(item: LenientBackhaulWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulWindowActiveGuardRule(var limit: Int) : LenientBackhaulWindowRule {
    override fun check(item: LenientBackhaulWindow): MutableList<String> {
        if (item.active && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulWindowRequiredSegmentRule(var message: String) : LenientBackhaulWindowRule {
    override fun check(item: LenientBackhaulWindow): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulWindowCodePrefixRule(var prefix: String) : LenientBackhaulWindowRule {
    override fun check(item: LenientBackhaulWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient backhaul window. */
data class LenientBackhaulWindowValidator(var rules: MutableList<LenientBackhaulWindowRule>) {
    fun validate(item: LenientBackhaulWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientBackhaulWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBackhaulWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBackhaulWindowValidator(): LenientBackhaulWindowValidator {
    return LenientBackhaulWindowValidator(
        mutableListOf<LenientBackhaulWindowRule>(
            LenientBackhaulWindowSegmentLengthRule(3600),
            LenientBackhaulWindowAllowedCodeRule(mutableListOf<String>("batched", "pending", "global")),
            LenientBackhaulWindowActiveGuardRule(120),
            LenientBackhaulWindowRequiredSegmentRule("lenient backhaul window needs a value"),
            LenientBackhaulWindowCodePrefixRule("Q"),
        ),
    )
}
