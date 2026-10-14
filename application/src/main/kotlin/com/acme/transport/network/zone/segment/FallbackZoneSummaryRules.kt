package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize

/** A rule a fallback zone summary must pass. Check returns its problems, or none. */
interface FallbackZoneSummaryRule {
    fun check(item: FallbackZoneSummary): MutableList<String>
}

data class FallbackZoneSummaryCeilingRangeRule(var min: Double, var max: Double) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneSummaryPinnedGuardRule(var limit: Double) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneSummaryTitleLengthRule(var maxLength: Int) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneSummaryRatioRangeRule(var min: Double, var max: Double) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class FallbackZoneSummaryCodeLengthRule(var maxLength: Int) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackZoneSummaryAllowedChecksumRule(var allowed: MutableList<String>) : FallbackZoneSummaryRule {
    override fun check(item: FallbackZoneSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback zone summary. */
data class FallbackZoneSummaryValidator(var rules: MutableList<FallbackZoneSummaryRule>) {
    fun validate(item: FallbackZoneSummary): MutableList<String> {
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

    fun requireValid(item: FallbackZoneSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackZoneSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackZoneSummaryValidator(): FallbackZoneSummaryValidator {
    return FallbackZoneSummaryValidator(
        mutableListOf<FallbackZoneSummaryRule>(
            FallbackZoneSummaryCeilingRangeRule(5.2, 1833.1),
            FallbackZoneSummaryPinnedGuardRule(4.8),
            FallbackZoneSummaryTitleLengthRule(255),
            FallbackZoneSummaryRatioRangeRule(8.6, 423.3),
            FallbackZoneSummaryCodeLengthRule(64),
            FallbackZoneSummaryAllowedChecksumRule(mutableListOf<String>("capped")),
        ),
    )
}
