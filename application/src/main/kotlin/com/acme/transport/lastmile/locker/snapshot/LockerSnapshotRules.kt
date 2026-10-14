package com.acme.transport.lastmile.locker.snapshot

/** A rule a locker snapshot must pass. Check returns its problems, or none. */
interface LockerSnapshotRule {
    fun check(item: LockerSnapshot): MutableList<String>
}

data class LockerSnapshotRequiredTitleRule(var message: String) : LockerSnapshotRule {
    override fun check(item: LockerSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerSnapshotEndsAtRangeRule(var min: Int, var max: Int) : LockerSnapshotRule {
    override fun check(item: LockerSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerSnapshotCodePrefixRule(var prefix: String) : LockerSnapshotRule {
    override fun check(item: LockerSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerSnapshotRequiredNotesRule(var message: String) : LockerSnapshotRule {
    override fun check(item: LockerSnapshot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker snapshot. */
data class LockerSnapshotValidator(var rules: MutableList<LockerSnapshotRule>) {
    fun validate(item: LockerSnapshot): MutableList<String> {
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

    fun requireValid(item: LockerSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerSnapshotValidator(): LockerSnapshotValidator {
    return LockerSnapshotValidator(
        mutableListOf<LockerSnapshotRule>(
            LockerSnapshotRequiredTitleRule("locker snapshot needs an entry"),
            LockerSnapshotEndsAtRangeRule(1000, 86400),
            LockerSnapshotCodePrefixRule("ACM"),
            LockerSnapshotRequiredNotesRule("locker snapshot needs this field"),
        ),
    )
}
