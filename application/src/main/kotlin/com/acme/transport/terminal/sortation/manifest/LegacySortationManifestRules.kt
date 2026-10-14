package com.acme.transport.terminal.sortation.manifest

/** A rule a legacy sortation manifest must pass. Check returns its problems, or none. */
interface LegacySortationManifestRule {
    fun check(item: LegacySortationManifest): MutableList<String>
}

data class LegacySortationManifestRequiredNotesRule(var message: String) : LegacySortationManifestRule {
    override fun check(item: LegacySortationManifest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationManifestExpiresAtRangeRule(var min: Int, var max: Int) : LegacySortationManifestRule {
    override fun check(item: LegacySortationManifest): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class LegacySortationManifestRequiredTimezoneRule(var message: String) : LegacySortationManifestRule {
    override fun check(item: LegacySortationManifest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation manifest. */
data class LegacySortationManifestValidator(var rules: MutableList<LegacySortationManifestRule>) {
    fun validate(item: LegacySortationManifest): MutableList<String> {
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

    fun requireValid(item: LegacySortationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationManifestValidator(): LegacySortationManifestValidator {
    return LegacySortationManifestValidator(
        mutableListOf<LegacySortationManifestRule>(
            LegacySortationManifestRequiredNotesRule("legacy sortation manifest needs an entry"),
            LegacySortationManifestExpiresAtRangeRule(16, 32),
            LegacySortationManifestRequiredTimezoneRule("legacy sortation manifest needs a value"),
        ),
    )
}
