package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize

/** A rule a hub segment must pass. Check returns its problems, or none. */
interface HubSegmentRule {
    fun check(item: HubSegment): MutableList<String>
}

data class HubSegmentRequiredContactEmailRule(var message: String) : HubSegmentRule {
    override fun check(item: HubSegment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubSegmentAllowedCodeRule(var allowed: MutableList<String>) : HubSegmentRule {
    override fun check(item: HubSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class HubSegmentCodeLengthRule(var maxLength: Int) : HubSegmentRule {
    override fun check(item: HubSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubSegmentRequiredCurrencyRule(var message: String) : HubSegmentRule {
    override fun check(item: HubSegment): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub segment. */
data class HubSegmentValidator(var rules: MutableList<HubSegmentRule>) {
    fun validate(item: HubSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultHubSegmentValidator(): HubSegmentValidator {
    return HubSegmentValidator(
        mutableListOf<HubSegmentRule>(
            HubSegmentRequiredContactEmailRule("hub segment needs this field"),
            HubSegmentAllowedCodeRule(mutableListOf<String>("strict")),
            HubSegmentCodeLengthRule(16),
            HubSegmentRequiredCurrencyRule("hub segment needs an entry"),
        ),
    )
}
