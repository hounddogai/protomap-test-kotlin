package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.text.normalize

/** A rule a dispute segment must pass. Check returns its problems, or none. */
interface DisputeSegmentRule {
    fun check(item: DisputeSegment): MutableList<String>
}

data class DisputeSegmentAllowedLocaleRule(var allowed: MutableList<String>) : DisputeSegmentRule {
    override fun check(item: DisputeSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSegmentLocaleLengthRule(var maxLength: Int) : DisputeSegmentRule {
    override fun check(item: DisputeSegment): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSegmentRequiredTitleRule(var message: String) : DisputeSegmentRule {
    override fun check(item: DisputeSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeSegmentOwnerIdLengthRule(var maxLength: Int) : DisputeSegmentRule {
    override fun check(item: DisputeSegment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeSegmentRequiredLocaleRule(var message: String) : DisputeSegmentRule {
    override fun check(item: DisputeSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute segment. */
data class DisputeSegmentValidator(var rules: MutableList<DisputeSegmentRule>) {
    fun validate(item: DisputeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeSegmentValidator(): DisputeSegmentValidator {
    return DisputeSegmentValidator(
        mutableListOf<DisputeSegmentRule>(
            DisputeSegmentAllowedLocaleRule(mutableListOf<String>("tiered")),
            DisputeSegmentLocaleLengthRule(1000),
            DisputeSegmentRequiredTitleRule("dispute segment needs a value"),
            DisputeSegmentOwnerIdLengthRule(64),
            DisputeSegmentRequiredLocaleRule("dispute segment needs this field"),
        ),
    )
}
