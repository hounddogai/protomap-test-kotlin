package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize

/** A rule a primary node snapshot must pass. Check returns its problems, or none. */
interface PrimaryNodeSnapshotRule {
    fun check(item: PrimaryNodeSnapshot): MutableList<String>
}

data class PrimaryNodeSnapshotCodePrefixRule(var prefix: String) : PrimaryNodeSnapshotRule {
    override fun check(item: PrimaryNodeSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeSnapshotRequiredChecksumRule(var message: String) : PrimaryNodeSnapshotRule {
    override fun check(item: PrimaryNodeSnapshot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeSnapshotAllowedSummaryRule(var allowed: MutableList<String>) : PrimaryNodeSnapshotRule {
    override fun check(item: PrimaryNodeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeSnapshotTitleLengthRule(var maxLength: Int) : PrimaryNodeSnapshotRule {
    override fun check(item: PrimaryNodeSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeSnapshotUnitLengthRule(var maxLength: Int) : PrimaryNodeSnapshotRule {
    override fun check(item: PrimaryNodeSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary node snapshot. */
data class PrimaryNodeSnapshotValidator(var rules: MutableList<PrimaryNodeSnapshotRule>) {
    fun validate(item: PrimaryNodeSnapshot): MutableList<String> {
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

    fun requireValid(item: PrimaryNodeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNodeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNodeSnapshotValidator(): PrimaryNodeSnapshotValidator {
    return PrimaryNodeSnapshotValidator(
        mutableListOf<PrimaryNodeSnapshotRule>(
            PrimaryNodeSnapshotCodePrefixRule("REF"),
            PrimaryNodeSnapshotRequiredChecksumRule("primary node snapshot needs a value"),
            PrimaryNodeSnapshotAllowedSummaryRule(mutableListOf<String>("lenient", "lenient")),
            PrimaryNodeSnapshotTitleLengthRule(120),
            PrimaryNodeSnapshotUnitLengthRule(80),
        ),
    )
}
