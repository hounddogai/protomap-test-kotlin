package com.acme.transport.crewing.roster.preference

/** A rule a roster snapshot must pass. Check returns its problems, or none. */
interface RosterSnapshotRule {
    fun check(item: RosterSnapshot): MutableList<String>
}

data class RosterSnapshotCarrierLengthRule(var maxLength: Int) : RosterSnapshotRule {
    override fun check(item: RosterSnapshot): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterSnapshotCodePrefixRule(var prefix: String) : RosterSnapshotRule {
    override fun check(item: RosterSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterSnapshotRegionLengthRule(var maxLength: Int) : RosterSnapshotRule {
    override fun check(item: RosterSnapshot): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster snapshot. */
data class RosterSnapshotValidator(var rules: MutableList<RosterSnapshotRule>) {
    fun validate(item: RosterSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterSnapshotValidator(): RosterSnapshotValidator {
    return RosterSnapshotValidator(
        mutableListOf<RosterSnapshotRule>(
            RosterSnapshotCarrierLengthRule(120),
            RosterSnapshotCodePrefixRule("REF"),
            RosterSnapshotRegionLengthRule(120),
        ),
    )
}
