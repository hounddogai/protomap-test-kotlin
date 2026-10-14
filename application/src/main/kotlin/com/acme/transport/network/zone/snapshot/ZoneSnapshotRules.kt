package com.acme.transport.network.zone.snapshot

/** A rule a zone snapshot must pass. Check returns its problems, or none. */
interface ZoneSnapshotRule {
    fun check(item: ZoneSnapshot): MutableList<String>
}

data class ZoneSnapshotSequenceRangeRule(var min: Int, var max: Int) : ZoneSnapshotRule {
    override fun check(item: ZoneSnapshot): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ZoneSnapshotCodeLengthRule(var maxLength: Int) : ZoneSnapshotRule {
    override fun check(item: ZoneSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneSnapshotRequiredTitleRule(var message: String) : ZoneSnapshotRule {
    override fun check(item: ZoneSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone snapshot. */
data class ZoneSnapshotValidator(var rules: MutableList<ZoneSnapshotRule>) {
    fun validate(item: ZoneSnapshot): MutableList<String> {
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

    fun requireValid(item: ZoneSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneSnapshotValidator(): ZoneSnapshotValidator {
    return ZoneSnapshotValidator(
        mutableListOf<ZoneSnapshotRule>(
            ZoneSnapshotSequenceRangeRule(120, 120),
            ZoneSnapshotCodeLengthRule(1000),
            ZoneSnapshotRequiredTitleRule("zone snapshot needs this field"),
        ),
    )
}
