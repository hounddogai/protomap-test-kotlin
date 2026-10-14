package com.acme.transport.linehaul.backhaul.backhaulreservation

/** A rule a legacy backhaul window must pass. Check returns its problems, or none. */
interface LegacyBackhaulWindowRule {
    fun check(item: LegacyBackhaulWindow): MutableList<String>
}

data class LegacyBackhaulWindowTitleLengthRule(var maxLength: Int) : LegacyBackhaulWindowRule {
    override fun check(item: LegacyBackhaulWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulWindowMinimumRangeRule(var min: Double, var max: Double) : LegacyBackhaulWindowRule {
    override fun check(item: LegacyBackhaulWindow): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulWindowPinnedGuardRule(var limit: Double) : LegacyBackhaulWindowRule {
    override fun check(item: LegacyBackhaulWindow): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul window. */
data class LegacyBackhaulWindowValidator(var rules: MutableList<LegacyBackhaulWindowRule>) {
    fun validate(item: LegacyBackhaulWindow): MutableList<String> {
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

    fun requireValid(item: LegacyBackhaulWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulWindowValidator(): LegacyBackhaulWindowValidator {
    return LegacyBackhaulWindowValidator(
        mutableListOf<LegacyBackhaulWindowRule>(
            LegacyBackhaulWindowTitleLengthRule(1000),
            LegacyBackhaulWindowMinimumRangeRule(6.2, 1701.9),
            LegacyBackhaulWindowPinnedGuardRule(2.9),
        ),
    )
}
