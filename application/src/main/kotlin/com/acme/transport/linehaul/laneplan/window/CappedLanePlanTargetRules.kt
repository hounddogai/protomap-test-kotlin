package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize

/** A rule a capped lane plan target must pass. Check returns its problems, or none. */
interface CappedLanePlanTargetRule {
    fun check(item: CappedLanePlanTarget): MutableList<String>
}

data class CappedLanePlanTargetCodePrefixRule(var prefix: String) : CappedLanePlanTargetRule {
    override fun check(item: CappedLanePlanTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanTargetAllowedLabelRule(var allowed: MutableList<String>) : CappedLanePlanTargetRule {
    override fun check(item: CappedLanePlanTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanTargetCodeLengthRule(var maxLength: Int) : CappedLanePlanTargetRule {
    override fun check(item: CappedLanePlanTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanTargetArchivedGuardRule(var limit: Double) : CappedLanePlanTargetRule {
    override fun check(item: CappedLanePlanTarget): MutableList<String> {
        if (item.archived && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanTargetScoreRangeRule(var min: Double, var max: Double) : CappedLanePlanTargetRule {
    override fun check(item: CappedLanePlanTarget): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped lane plan target. */
data class CappedLanePlanTargetValidator(var rules: MutableList<CappedLanePlanTargetRule>) {
    fun validate(item: CappedLanePlanTarget): MutableList<String> {
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

    fun requireValid(item: CappedLanePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLanePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLanePlanTargetValidator(): CappedLanePlanTargetValidator {
    return CappedLanePlanTargetValidator(
        mutableListOf<CappedLanePlanTargetRule>(
            CappedLanePlanTargetCodePrefixRule("ACM"),
            CappedLanePlanTargetAllowedLabelRule(mutableListOf<String>()),
            CappedLanePlanTargetCodeLengthRule(16),
            CappedLanePlanTargetArchivedGuardRule(7.7),
            CappedLanePlanTargetScoreRangeRule(4.7, 2658.3),
        ),
    )
}
