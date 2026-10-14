package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.text.normalize

/** A rule a capped redelivery window must pass. Check returns its problems, or none. */
interface CappedRedeliveryWindowRule {
    fun check(item: CappedRedeliveryWindow): MutableList<String>
}

data class CappedRedeliveryWindowChecksumLengthRule(var maxLength: Int) : CappedRedeliveryWindowRule {
    override fun check(item: CappedRedeliveryWindow): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRedeliveryWindowLabelLengthRule(var maxLength: Int) : CappedRedeliveryWindowRule {
    override fun check(item: CappedRedeliveryWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRedeliveryWindowCodePrefixRule(var prefix: String) : CappedRedeliveryWindowRule {
    override fun check(item: CappedRedeliveryWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRedeliveryWindowAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedRedeliveryWindowRule {
    override fun check(item: CappedRedeliveryWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped redelivery window. */
data class CappedRedeliveryWindowValidator(var rules: MutableList<CappedRedeliveryWindowRule>) {
    fun validate(item: CappedRedeliveryWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRedeliveryWindowValidator(): CappedRedeliveryWindowValidator {
    return CappedRedeliveryWindowValidator(
        mutableListOf<CappedRedeliveryWindowRule>(
            CappedRedeliveryWindowChecksumLengthRule(64),
            CappedRedeliveryWindowLabelLengthRule(32),
            CappedRedeliveryWindowCodePrefixRule("Q"),
            CappedRedeliveryWindowAllowedLabelRule(mutableListOf<String>("primary", "weighted", "seasonal")),
        ),
    )
}
