package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.text.normalize

/** A rule a lenient relay score must pass. Check returns its problems, or none. */
interface LenientRelayScoreRule {
    fun check(item: LenientRelayScore): MutableList<String>
}

data class LenientRelayScoreRequiredDockRule(var message: String) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayScoreMinimumRangeRule(var min: Double, var max: Double) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayScoreAllowedContactEmailRule(var allowed: MutableList<String>) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayScorePinnedGuardRule(var limit: Double) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        if (item.pinned && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayScoreRequiredLabelRule(var message: String) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayScoreDockLengthRule(var maxLength: Int) : LenientRelayScoreRule {
    override fun check(item: LenientRelayScore): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient relay score. */
data class LenientRelayScoreValidator(var rules: MutableList<LenientRelayScoreRule>) {
    fun validate(item: LenientRelayScore): MutableList<String> {
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

    fun requireValid(item: LenientRelayScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelayScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelayScoreValidator(): LenientRelayScoreValidator {
    return LenientRelayScoreValidator(
        mutableListOf<LenientRelayScoreRule>(
            LenientRelayScoreRequiredDockRule("lenient relay score needs a value"),
            LenientRelayScoreMinimumRangeRule(5.0, 4613.0),
            LenientRelayScoreAllowedContactEmailRule(mutableListOf<String>("global")),
            LenientRelayScorePinnedGuardRule(3.7),
            LenientRelayScoreRequiredLabelRule("lenient relay score needs an entry"),
            LenientRelayScoreDockLengthRule(80),
        ),
    )
}
