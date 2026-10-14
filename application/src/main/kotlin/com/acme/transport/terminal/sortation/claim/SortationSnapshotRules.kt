package com.acme.transport.terminal.sortation.claim

/** A rule a sortation snapshot must pass. Check returns its problems, or none. */
interface SortationSnapshotRule {
    fun check(item: SortationSnapshot): MutableList<String>
}

data class SortationSnapshotCodePrefixRule(var prefix: String) : SortationSnapshotRule {
    override fun check(item: SortationSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationSnapshotOwnerIdLengthRule(var maxLength: Int) : SortationSnapshotRule {
    override fun check(item: SortationSnapshot): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationSnapshotRequiredNameRule(var message: String) : SortationSnapshotRule {
    override fun check(item: SortationSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationSnapshotLaneLengthRule(var maxLength: Int) : SortationSnapshotRule {
    override fun check(item: SortationSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation snapshot. */
data class SortationSnapshotValidator(var rules: MutableList<SortationSnapshotRule>) {
    fun validate(item: SortationSnapshot): MutableList<String> {
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

    fun requireValid(item: SortationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationSnapshotValidator(): SortationSnapshotValidator {
    return SortationSnapshotValidator(
        mutableListOf<SortationSnapshotRule>(
            SortationSnapshotCodePrefixRule("REF"),
            SortationSnapshotOwnerIdLengthRule(64),
            SortationSnapshotRequiredNameRule("sortation snapshot needs an entry"),
            SortationSnapshotLaneLengthRule(120),
        ),
    )
}
