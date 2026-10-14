package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.text.normalize

/** A rule a global redelivery snapshot must pass. Check returns its problems, or none. */
interface GlobalRedeliverySnapshotRule {
    fun check(item: GlobalRedeliverySnapshot): MutableList<String>
}

data class GlobalRedeliverySnapshotExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliverySnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliverySnapshotCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class GlobalRedeliverySnapshotRequiredRegionRule(var message: String) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliverySnapshotRegionLengthRule(var maxLength: Int) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliverySnapshotCodeLengthRule(var maxLength: Int) : GlobalRedeliverySnapshotRule {
    override fun check(item: GlobalRedeliverySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global redelivery snapshot. */
data class GlobalRedeliverySnapshotValidator(var rules: MutableList<GlobalRedeliverySnapshotRule>) {
    fun validate(item: GlobalRedeliverySnapshot): MutableList<String> {
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

    fun requireValid(item: GlobalRedeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRedeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRedeliverySnapshotValidator(): GlobalRedeliverySnapshotValidator {
    return GlobalRedeliverySnapshotValidator(
        mutableListOf<GlobalRedeliverySnapshotRule>(
            GlobalRedeliverySnapshotExpiresAtRangeRule(16, 255),
            GlobalRedeliverySnapshotAllowedCodeRule(mutableListOf<String>("strict", "scheduled")),
            GlobalRedeliverySnapshotCreatedAtRangeRule(120, 86400),
            GlobalRedeliverySnapshotRequiredRegionRule("global redelivery snapshot needs an entry"),
            GlobalRedeliverySnapshotRegionLengthRule(120),
            GlobalRedeliverySnapshotCodeLengthRule(86400),
        ),
    )
}
