package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize

/** A rule a zone override must pass. Check returns its problems, or none. */
interface ZoneOverrideRule {
    fun check(item: ZoneOverride): MutableList<String>
}

data class ZoneOverrideRequiredTitleRule(var message: String) : ZoneOverrideRule {
    override fun check(item: ZoneOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneOverrideEtaMinutesRangeRule(var min: Int, var max: Int) : ZoneOverrideRule {
    override fun check(item: ZoneOverride): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneOverrideAllowedReviewerRule(var allowed: MutableList<String>) : ZoneOverrideRule {
    override fun check(item: ZoneOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ZoneOverrideTitleLengthRule(var maxLength: Int) : ZoneOverrideRule {
    override fun check(item: ZoneOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone override. */
data class ZoneOverrideValidator(var rules: MutableList<ZoneOverrideRule>) {
    fun validate(item: ZoneOverride): MutableList<String> {
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

    fun requireValid(item: ZoneOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneOverrideValidator(): ZoneOverrideValidator {
    return ZoneOverrideValidator(
        mutableListOf<ZoneOverrideRule>(
            ZoneOverrideRequiredTitleRule("zone override needs this field"),
            ZoneOverrideEtaMinutesRangeRule(16, 64),
            ZoneOverrideAllowedReviewerRule(mutableListOf<String>("default")),
            ZoneOverrideTitleLengthRule(64),
        ),
    )
}
