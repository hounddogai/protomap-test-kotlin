package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.normalize

/** A rule a pending dispute baseline must pass. Check returns its problems, or none. */
interface PendingDisputeBaselineRule {
    fun check(item: PendingDisputeBaseline): MutableList<String>
}

data class PendingDisputeBaselineRequiredLabelRule(var message: String) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeBaselineCodePrefixRule(var prefix: String) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeBaselineAllowedDepotRule(
    var allowed: MutableList<String>,
) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeBaselineCodeLengthRule(var maxLength: Int) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeBaselineDescriptionLengthRule(var maxLength: Int) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeBaselineRequiredDepotRule(var message: String) : PendingDisputeBaselineRule {
    override fun check(item: PendingDisputeBaseline): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dispute baseline. */
data class PendingDisputeBaselineValidator(var rules: MutableList<PendingDisputeBaselineRule>) {
    fun validate(item: PendingDisputeBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDisputeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDisputeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDisputeBaselineValidator(): PendingDisputeBaselineValidator {
    return PendingDisputeBaselineValidator(
        mutableListOf<PendingDisputeBaselineRule>(
            PendingDisputeBaselineRequiredLabelRule("pending dispute baseline needs a value"),
            PendingDisputeBaselineCodePrefixRule("Q"),
            PendingDisputeBaselineAllowedDepotRule(mutableListOf<String>("global")),
            PendingDisputeBaselineCodeLengthRule(64),
            PendingDisputeBaselineDescriptionLengthRule(64),
            PendingDisputeBaselineRequiredDepotRule("pending dispute baseline needs this field"),
        ),
    )
}
