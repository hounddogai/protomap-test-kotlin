package com.acme.transport.network.zone.cycle

/** A rule a zone summary must pass. Check returns its problems, or none. */
interface ZoneSummaryRule {
    fun check(item: ZoneSummary): MutableList<String>
}

data class ZoneSummaryExternalRefLengthRule(var maxLength: Int) : ZoneSummaryRule {
    override fun check(item: ZoneSummary): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneSummaryRateRangeRule(var min: Double, var max: Double) : ZoneSummaryRule {
    override fun check(item: ZoneSummary): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneSummaryTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : ZoneSummaryRule {
    override fun check(item: ZoneSummary): MutableList<String> {
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

data class ZoneSummaryCapacityRangeRule(var min: Int, var max: Int) : ZoneSummaryRule {
    override fun check(item: ZoneSummary): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone summary. */
data class ZoneSummaryValidator(var rules: MutableList<ZoneSummaryRule>) {
    fun validate(item: ZoneSummary): MutableList<String> {
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

    fun requireValid(item: ZoneSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneSummaryValidator(): ZoneSummaryValidator {
    return ZoneSummaryValidator(
        mutableListOf<ZoneSummaryRule>(
            ZoneSummaryExternalRefLengthRule(64),
            ZoneSummaryRateRangeRule(0.9, 1537.4),
            ZoneSummaryTagsLimitRule(80, mutableListOf<String>("lenient", "fallback", "capped")),
            ZoneSummaryCapacityRangeRule(32, 255),
        ),
    )
}
