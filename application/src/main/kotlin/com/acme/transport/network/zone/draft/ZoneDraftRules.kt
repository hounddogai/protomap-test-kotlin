package com.acme.transport.network.zone.draft

/** A rule a zone draft must pass. Check returns its problems, or none. */
interface ZoneDraftRule {
    fun check(item: ZoneDraft): MutableList<String>
}

data class ZoneDraftEndsAtRangeRule(var min: Int, var max: Int) : ZoneDraftRule {
    override fun check(item: ZoneDraft): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ZoneDraftNotesLengthRule(var maxLength: Int) : ZoneDraftRule {
    override fun check(item: ZoneDraft): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneDraftRequiredTitleRule(var message: String) : ZoneDraftRule {
    override fun check(item: ZoneDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone draft. */
data class ZoneDraftValidator(var rules: MutableList<ZoneDraftRule>) {
    fun validate(item: ZoneDraft): MutableList<String> {
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

    fun requireValid(item: ZoneDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneDraftValidator(): ZoneDraftValidator {
    return ZoneDraftValidator(
        mutableListOf<ZoneDraftRule>(
            ZoneDraftEndsAtRangeRule(32, 86400),
            ZoneDraftNotesLengthRule(32),
            ZoneDraftRequiredTitleRule("zone draft needs a value"),
        ),
    )
}
