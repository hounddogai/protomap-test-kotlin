package com.acme.transport.terminal.dwell.snapshot

/** A rule a lenient dwell policy must pass. Check returns its problems, or none. */
interface LenientDwellPolicyRule {
    fun check(item: LenientDwellPolicy): MutableList<String>
}

data class LenientDwellPolicyCodePrefixRule(var prefix: String) : LenientDwellPolicyRule {
    override fun check(item: LenientDwellPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellPolicyCodeLengthRule(var maxLength: Int) : LenientDwellPolicyRule {
    override fun check(item: LenientDwellPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellPolicyWindowRule(var maxSpan: Int) : LenientDwellPolicyRule {
    override fun check(item: LenientDwellPolicy): MutableList<String> {
        if (item.startsAt > item.endsAt) {
            return mutableListOf<String>("starts at is after ends at")
        }
        if (maxSpan > 0 && item.endsAt - item.startsAt > maxSpan) {
            return mutableListOf<String>("spans more than $maxSpan seconds")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellPolicyRequiredSegmentRule(var message: String) : LenientDwellPolicyRule {
    override fun check(item: LenientDwellPolicy): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellPolicyCurrencyLengthRule(var maxLength: Int) : LenientDwellPolicyRule {
    override fun check(item: LenientDwellPolicy): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell policy. */
data class LenientDwellPolicyValidator(var rules: MutableList<LenientDwellPolicyRule>) {
    fun validate(item: LenientDwellPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDwellPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellPolicyValidator(): LenientDwellPolicyValidator {
    return LenientDwellPolicyValidator(
        mutableListOf<LenientDwellPolicyRule>(
            LenientDwellPolicyCodePrefixRule("X"),
            LenientDwellPolicyCodeLengthRule(86400),
            LenientDwellPolicyWindowRule(1000),
            LenientDwellPolicyRequiredSegmentRule("lenient dwell policy needs a value"),
            LenientDwellPolicyCurrencyLengthRule(1000),
        ),
    )
}
