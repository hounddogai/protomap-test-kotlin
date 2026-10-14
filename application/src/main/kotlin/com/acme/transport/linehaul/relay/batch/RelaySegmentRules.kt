package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize

/** A rule a relay segment must pass. Check returns its problems, or none. */
interface RelaySegmentRule {
    fun check(item: RelaySegment): MutableList<String>
}

data class RelaySegmentRequiredCodeRule(var message: String) : RelaySegmentRule {
    override fun check(item: RelaySegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelaySegmentCodePrefixRule(var prefix: String) : RelaySegmentRule {
    override fun check(item: RelaySegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelaySegmentRequiredDriverIdRule(var message: String) : RelaySegmentRule {
    override fun check(item: RelaySegment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelaySegmentCodeLengthRule(var maxLength: Int) : RelaySegmentRule {
    override fun check(item: RelaySegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelaySegmentAllowedDriverIdRule(var allowed: MutableList<String>) : RelaySegmentRule {
    override fun check(item: RelaySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay segment. */
data class RelaySegmentValidator(var rules: MutableList<RelaySegmentRule>) {
    fun validate(item: RelaySegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelaySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelaySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRelaySegmentValidator(): RelaySegmentValidator {
    return RelaySegmentValidator(
        mutableListOf<RelaySegmentRule>(
            RelaySegmentRequiredCodeRule("relay segment needs a value"),
            RelaySegmentCodePrefixRule("X"),
            RelaySegmentRequiredDriverIdRule("relay segment needs an entry"),
            RelaySegmentCodeLengthRule(80),
            RelaySegmentAllowedDriverIdRule(mutableListOf<String>()),
        ),
    )
}
