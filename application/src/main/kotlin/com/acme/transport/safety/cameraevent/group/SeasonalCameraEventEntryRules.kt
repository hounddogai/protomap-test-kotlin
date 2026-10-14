package com.acme.transport.safety.cameraevent.group

/** A rule a seasonal camera event entry must pass. Check returns its problems, or none. */
interface SeasonalCameraEventEntryRule {
    fun check(item: SeasonalCameraEventEntry): MutableList<String>
}

data class SeasonalCameraEventEntryEndsAtRangeRule(var min: Int, var max: Int) : SeasonalCameraEventEntryRule {
    override fun check(item: SeasonalCameraEventEntry): MutableList<String> {
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

data class SeasonalCameraEventEntryRequiredOwnerIdRule(var message: String) : SeasonalCameraEventEntryRule {
    override fun check(item: SeasonalCameraEventEntry): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCameraEventEntryTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SeasonalCameraEventEntryRule {
    override fun check(item: SeasonalCameraEventEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class SeasonalCameraEventEntryRequiredExternalRefRule(var message: String) : SeasonalCameraEventEntryRule {
    override fun check(item: SeasonalCameraEventEntry): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal camera event entry. */
data class SeasonalCameraEventEntryValidator(var rules: MutableList<SeasonalCameraEventEntryRule>) {
    fun validate(item: SeasonalCameraEventEntry): MutableList<String> {
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

    fun requireValid(item: SeasonalCameraEventEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCameraEventEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCameraEventEntryValidator(): SeasonalCameraEventEntryValidator {
    return SeasonalCameraEventEntryValidator(
        mutableListOf<SeasonalCameraEventEntryRule>(
            SeasonalCameraEventEntryEndsAtRangeRule(120, 86400),
            SeasonalCameraEventEntryRequiredOwnerIdRule("seasonal camera event entry needs a value"),
            SeasonalCameraEventEntryTagsLimitRule(1000, mutableListOf<String>("primary")),
            SeasonalCameraEventEntryRequiredExternalRefRule("seasonal camera event entry needs this field"),
        ),
    )
}
