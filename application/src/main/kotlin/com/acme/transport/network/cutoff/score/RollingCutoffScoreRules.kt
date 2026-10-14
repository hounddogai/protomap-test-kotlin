package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize

/** A rule a rolling cutoff score must pass. Check returns its problems, or none. */
interface RollingCutoffScoreRule {
    fun check(item: RollingCutoffScore): MutableList<String>
}

data class RollingCutoffScoreLabelLengthRule(var maxLength: Int) : RollingCutoffScoreRule {
    override fun check(item: RollingCutoffScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffScoreRequiredCodeRule(var message: String) : RollingCutoffScoreRule {
    override fun check(item: RollingCutoffScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffScorePalletsRangeRule(var min: Int, var max: Int) : RollingCutoffScoreRule {
    override fun check(item: RollingCutoffScore): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffScoreSegmentLengthRule(var maxLength: Int) : RollingCutoffScoreRule {
    override fun check(item: RollingCutoffScore): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffScoreAllowedLabelRule(var allowed: MutableList<String>) : RollingCutoffScoreRule {
    override fun check(item: RollingCutoffScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff score. */
data class RollingCutoffScoreValidator(var rules: MutableList<RollingCutoffScoreRule>) {
    fun validate(item: RollingCutoffScore): MutableList<String> {
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

    fun requireValid(item: RollingCutoffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffScoreValidator(): RollingCutoffScoreValidator {
    return RollingCutoffScoreValidator(
        mutableListOf<RollingCutoffScoreRule>(
            RollingCutoffScoreLabelLengthRule(80),
            RollingCutoffScoreRequiredCodeRule("rolling cutoff score needs this field"),
            RollingCutoffScorePalletsRangeRule(80, 255),
            RollingCutoffScoreSegmentLengthRule(255),
            RollingCutoffScoreAllowedLabelRule(mutableListOf<String>("strict")),
        ),
    )
}
