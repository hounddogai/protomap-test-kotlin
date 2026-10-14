package com.acme.transport.fleetops.tire.batch

/** A rule a capped tire snapshot must pass. Check returns its problems, or none. */
interface CappedTireSnapshotRule {
    fun check(item: CappedTireSnapshot): MutableList<String>
}

data class CappedTireSnapshotTrailerIdLengthRule(var maxLength: Int) : CappedTireSnapshotRule {
    override fun check(item: CappedTireSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSnapshotRequiredCodeRule(var message: String) : CappedTireSnapshotRule {
    override fun check(item: CappedTireSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSnapshotRequiredCountryRule(var message: String) : CappedTireSnapshotRule {
    override fun check(item: CappedTireSnapshot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tire snapshot. */
data class CappedTireSnapshotValidator(var rules: MutableList<CappedTireSnapshotRule>) {
    fun validate(item: CappedTireSnapshot): MutableList<String> {
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

    fun requireValid(item: CappedTireSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTireSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTireSnapshotValidator(): CappedTireSnapshotValidator {
    return CappedTireSnapshotValidator(
        mutableListOf<CappedTireSnapshotRule>(
            CappedTireSnapshotTrailerIdLengthRule(255),
            CappedTireSnapshotRequiredCodeRule("capped tire snapshot needs a value"),
            CappedTireSnapshotRequiredCountryRule("capped tire snapshot needs an entry"),
        ),
    )
}
