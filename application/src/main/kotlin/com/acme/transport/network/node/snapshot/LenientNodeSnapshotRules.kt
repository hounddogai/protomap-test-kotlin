package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a lenient node snapshot must pass. Check returns its problems, or none. */
interface LenientNodeSnapshotRule {
    fun check(item: LenientNodeSnapshot): MutableList<String>
}

data class LenientNodeSnapshotAllowedCodeRule(var allowed: MutableList<String>) : LenientNodeSnapshotRule {
    override fun check(item: LenientNodeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeSnapshotCodeLengthRule(var maxLength: Int) : LenientNodeSnapshotRule {
    override fun check(item: LenientNodeSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeSnapshotRequiredCodeRule(var message: String) : LenientNodeSnapshotRule {
    override fun check(item: LenientNodeSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeSnapshotSegmentLengthRule(var maxLength: Int) : LenientNodeSnapshotRule {
    override fun check(item: LenientNodeSnapshot): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeSnapshotNameLengthRule(var maxLength: Int) : LenientNodeSnapshotRule {
    override fun check(item: LenientNodeSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient node snapshot. */
data class LenientNodeSnapshotValidator(var rules: MutableList<LenientNodeSnapshotRule>) {
    fun validate(item: LenientNodeSnapshot): MutableList<String> {
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

    fun requireValid(item: LenientNodeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNodeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNodeSnapshotValidator(): LenientNodeSnapshotValidator {
    return LenientNodeSnapshotValidator(
        mutableListOf<LenientNodeSnapshotRule>(
            LenientNodeSnapshotAllowedCodeRule(mutableListOf<String>("strict", "scheduled", "default")),
            LenientNodeSnapshotCodeLengthRule(255),
            LenientNodeSnapshotRequiredCodeRule("lenient node snapshot needs this field"),
            LenientNodeSnapshotSegmentLengthRule(16),
            LenientNodeSnapshotNameLengthRule(3600),
        ),
    )
}
