package com.acme.transport.safety.violation.snapshot

/** A rule a pending violation snapshot must pass. Check returns its problems, or none. */
interface PendingViolationSnapshotRule {
    fun check(item: PendingViolationSnapshot): MutableList<String>
}

data class PendingViolationSnapshotRequiredCodeRule(var message: String) : PendingViolationSnapshotRule {
    override fun check(item: PendingViolationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationSnapshotEndsAtRangeRule(var min: Int, var max: Int) : PendingViolationSnapshotRule {
    override fun check(item: PendingViolationSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationSnapshotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PendingViolationSnapshotRule {
    override fun check(item: PendingViolationSnapshot): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PendingViolationSnapshotRequiredNameRule(var message: String) : PendingViolationSnapshotRule {
    override fun check(item: PendingViolationSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationSnapshotRequiredLabelRule(var message: String) : PendingViolationSnapshotRule {
    override fun check(item: PendingViolationSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending violation snapshot. */
data class PendingViolationSnapshotValidator(var rules: MutableList<PendingViolationSnapshotRule>) {
    fun validate(item: PendingViolationSnapshot): MutableList<String> {
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

    fun requireValid(item: PendingViolationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingViolationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingViolationSnapshotValidator(): PendingViolationSnapshotValidator {
    return PendingViolationSnapshotValidator(
        mutableListOf<PendingViolationSnapshotRule>(
            PendingViolationSnapshotRequiredCodeRule("pending violation snapshot needs this field"),
            PendingViolationSnapshotEndsAtRangeRule(1000, 3600),
            PendingViolationSnapshotEtaMinutesRangeRule(3600, 86400),
            PendingViolationSnapshotRequiredNameRule("pending violation snapshot needs this field"),
            PendingViolationSnapshotRequiredLabelRule("pending violation snapshot needs an entry"),
        ),
    )
}
