package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional node snapshot must pass. Check returns its problems, or none. */
interface RegionalNodeSnapshotRule {
    fun check(item: RegionalNodeSnapshot): MutableList<String>
}

data class RegionalNodeSnapshotCarrierLengthRule(var maxLength: Int) : RegionalNodeSnapshotRule {
    override fun check(item: RegionalNodeSnapshot): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeSnapshotAllowedPhoneRule(var allowed: MutableList<String>) : RegionalNodeSnapshotRule {
    override fun check(item: RegionalNodeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeSnapshotRequiredCodeRule(var message: String) : RegionalNodeSnapshotRule {
    override fun check(item: RegionalNodeSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeSnapshotSequenceRangeRule(var min: Int, var max: Int) : RegionalNodeSnapshotRule {
    override fun check(item: RegionalNodeSnapshot): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeSnapshotAttemptsRangeRule(var min: Int, var max: Int) : RegionalNodeSnapshotRule {
    override fun check(item: RegionalNodeSnapshot): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional node snapshot. */
data class RegionalNodeSnapshotValidator(var rules: MutableList<RegionalNodeSnapshotRule>) {
    fun validate(item: RegionalNodeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalNodeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalNodeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalNodeSnapshotValidator(): RegionalNodeSnapshotValidator {
    return RegionalNodeSnapshotValidator(
        mutableListOf<RegionalNodeSnapshotRule>(
            RegionalNodeSnapshotCarrierLengthRule(80),
            RegionalNodeSnapshotAllowedPhoneRule(mutableListOf<String>("fallback", "regional")),
            RegionalNodeSnapshotRequiredCodeRule("regional node snapshot needs this field"),
            RegionalNodeSnapshotSequenceRangeRule(120, 86400),
            RegionalNodeSnapshotAttemptsRangeRule(80, 120),
        ),
    )
}
