package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a pending swap snapshot must pass. Check returns its problems, or none. */
interface PendingSwapSnapshotRule {
    fun check(item: PendingSwapSnapshot): MutableList<String>
}

data class PendingSwapSnapshotAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : PendingSwapSnapshotRule {
    override fun check(item: PendingSwapSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapSnapshotRequiredExternalRefRule(var message: String) : PendingSwapSnapshotRule {
    override fun check(item: PendingSwapSnapshot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapSnapshotRequiredCodeRule(var message: String) : PendingSwapSnapshotRule {
    override fun check(item: PendingSwapSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSwapSnapshotCodeLengthRule(var maxLength: Int) : PendingSwapSnapshotRule {
    override fun check(item: PendingSwapSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending swap snapshot. */
data class PendingSwapSnapshotValidator(var rules: MutableList<PendingSwapSnapshotRule>) {
    fun validate(item: PendingSwapSnapshot): MutableList<String> {
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

    fun requireValid(item: PendingSwapSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSwapSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSwapSnapshotValidator(): PendingSwapSnapshotValidator {
    return PendingSwapSnapshotValidator(
        mutableListOf<PendingSwapSnapshotRule>(
            PendingSwapSnapshotAllowedExternalRefRule(mutableListOf<String>("regional", "global", "legacy")),
            PendingSwapSnapshotRequiredExternalRefRule("pending swap snapshot needs this field"),
            PendingSwapSnapshotRequiredCodeRule("pending swap snapshot needs a value"),
            PendingSwapSnapshotCodeLengthRule(3600),
        ),
    )
}
