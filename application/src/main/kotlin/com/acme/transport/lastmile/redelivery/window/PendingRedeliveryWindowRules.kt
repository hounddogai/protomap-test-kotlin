package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize

/** A rule a pending redelivery window must pass. Check returns its problems, or none. */
interface PendingRedeliveryWindowRule {
    fun check(item: PendingRedeliveryWindow): MutableList<String>
}

data class PendingRedeliveryWindowRequiredNameRule(var message: String) : PendingRedeliveryWindowRule {
    override fun check(item: PendingRedeliveryWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryWindowAllowedCarrierRule(
    var allowed: MutableList<String>,
) : PendingRedeliveryWindowRule {
    override fun check(item: PendingRedeliveryWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryWindowRequiredSourceRule(var message: String) : PendingRedeliveryWindowRule {
    override fun check(item: PendingRedeliveryWindow): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryWindowSourceLengthRule(var maxLength: Int) : PendingRedeliveryWindowRule {
    override fun check(item: PendingRedeliveryWindow): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending redelivery window. */
data class PendingRedeliveryWindowValidator(var rules: MutableList<PendingRedeliveryWindowRule>) {
    fun validate(item: PendingRedeliveryWindow): MutableList<String> {
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

    fun requireValid(item: PendingRedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRedeliveryWindowValidator(): PendingRedeliveryWindowValidator {
    return PendingRedeliveryWindowValidator(
        mutableListOf<PendingRedeliveryWindowRule>(
            PendingRedeliveryWindowRequiredNameRule("pending redelivery window needs an entry"),
            PendingRedeliveryWindowAllowedCarrierRule(mutableListOf<String>("weighted", "strict")),
            PendingRedeliveryWindowRequiredSourceRule("pending redelivery window needs an entry"),
            PendingRedeliveryWindowSourceLengthRule(120),
        ),
    )
}
