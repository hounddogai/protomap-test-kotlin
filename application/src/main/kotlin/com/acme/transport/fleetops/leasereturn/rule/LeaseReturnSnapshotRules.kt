package com.acme.transport.fleetops.leasereturn.rule

/** A rule a lease return snapshot must pass. Check returns its problems, or none. */
interface LeaseReturnSnapshotRule {
    fun check(item: LeaseReturnSnapshot): MutableList<String>
}

data class LeaseReturnSnapshotDurationMinutesRangeRule(var min: Int, var max: Int) : LeaseReturnSnapshotRule {
    override fun check(item: LeaseReturnSnapshot): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnSnapshotNotesLengthRule(var maxLength: Int) : LeaseReturnSnapshotRule {
    override fun check(item: LeaseReturnSnapshot): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSnapshotRequiredNotesRule(var message: String) : LeaseReturnSnapshotRule {
    override fun check(item: LeaseReturnSnapshot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnSnapshotLabelLengthRule(var maxLength: Int) : LeaseReturnSnapshotRule {
    override fun check(item: LeaseReturnSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return snapshot. */
data class LeaseReturnSnapshotValidator(var rules: MutableList<LeaseReturnSnapshotRule>) {
    fun validate(item: LeaseReturnSnapshot): MutableList<String> {
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

    fun requireValid(item: LeaseReturnSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnSnapshotValidator(): LeaseReturnSnapshotValidator {
    return LeaseReturnSnapshotValidator(
        mutableListOf<LeaseReturnSnapshotRule>(
            LeaseReturnSnapshotDurationMinutesRangeRule(16, 255),
            LeaseReturnSnapshotNotesLengthRule(86400),
            LeaseReturnSnapshotRequiredNotesRule("lease return snapshot needs this field"),
            LeaseReturnSnapshotLabelLengthRule(3600),
        ),
    )
}
