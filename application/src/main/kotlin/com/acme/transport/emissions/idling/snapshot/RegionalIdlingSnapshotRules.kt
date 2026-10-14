package com.acme.transport.emissions.idling.snapshot

/** A rule a regional idling snapshot must pass. Check returns its problems, or none. */
interface RegionalIdlingSnapshotRule {
    fun check(item: RegionalIdlingSnapshot): MutableList<String>
}

data class RegionalIdlingSnapshotCodeLengthRule(var maxLength: Int) : RegionalIdlingSnapshotRule {
    override fun check(item: RegionalIdlingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingSnapshotRequiredContactEmailRule(var message: String) : RegionalIdlingSnapshotRule {
    override fun check(item: RegionalIdlingSnapshot): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIdlingSnapshotRequiredCodeRule(var message: String) : RegionalIdlingSnapshotRule {
    override fun check(item: RegionalIdlingSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional idling snapshot. */
data class RegionalIdlingSnapshotValidator(var rules: MutableList<RegionalIdlingSnapshotRule>) {
    fun validate(item: RegionalIdlingSnapshot): MutableList<String> {
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

    fun requireValid(item: RegionalIdlingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIdlingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIdlingSnapshotValidator(): RegionalIdlingSnapshotValidator {
    return RegionalIdlingSnapshotValidator(
        mutableListOf<RegionalIdlingSnapshotRule>(
            RegionalIdlingSnapshotCodeLengthRule(80),
            RegionalIdlingSnapshotRequiredContactEmailRule("regional idling snapshot needs a value"),
            RegionalIdlingSnapshotRequiredCodeRule("regional idling snapshot needs a value"),
        ),
    )
}
