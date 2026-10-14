package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize

/** A rule a rolling rest break entry must pass. Check returns its problems, or none. */
interface RollingRestBreakEntryRule {
    fun check(item: RollingRestBreakEntry): MutableList<String>
}

data class RollingRestBreakEntryGraceDaysRangeRule(var min: Int, var max: Int) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakEntryContactEmailLengthRule(var maxLength: Int) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakEntryLabelLengthRule(var maxLength: Int) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakEntryAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakEntryRevisionRangeRule(var min: Int, var max: Int) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRestBreakEntryCreatedAtRangeRule(var min: Int, var max: Int) : RollingRestBreakEntryRule {
    override fun check(item: RollingRestBreakEntry): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling rest break entry. */
data class RollingRestBreakEntryValidator(var rules: MutableList<RollingRestBreakEntryRule>) {
    fun validate(item: RollingRestBreakEntry): MutableList<String> {
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

    fun requireValid(item: RollingRestBreakEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRestBreakEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRestBreakEntryValidator(): RollingRestBreakEntryValidator {
    return RollingRestBreakEntryValidator(
        mutableListOf<RollingRestBreakEntryRule>(
            RollingRestBreakEntryGraceDaysRangeRule(120, 255),
            RollingRestBreakEntryContactEmailLengthRule(80),
            RollingRestBreakEntryLabelLengthRule(255),
            RollingRestBreakEntryAllowedExternalRefRule(mutableListOf<String>("global", "seasonal", "global")),
            RollingRestBreakEntryRevisionRangeRule(32, 32),
            RollingRestBreakEntryCreatedAtRangeRule(80, 3600),
        ),
    )
}
