package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.normalize

/** A rule a seasonal relay notice must pass. Check returns its problems, or none. */
interface SeasonalRelayNoticeRule {
    fun check(item: SeasonalRelayNotice): MutableList<String>
}

data class SeasonalRelayNoticeThresholdRangeRule(var min: Double, var max: Double) : SeasonalRelayNoticeRule {
    override fun check(item: SeasonalRelayNotice): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class SeasonalRelayNoticeAllowedCodeRule(var allowed: MutableList<String>) : SeasonalRelayNoticeRule {
    override fun check(item: SeasonalRelayNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayNoticeLabelLengthRule(var maxLength: Int) : SeasonalRelayNoticeRule {
    override fun check(item: SeasonalRelayNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayNoticeFloorValueRangeRule(var min: Double, var max: Double) : SeasonalRelayNoticeRule {
    override fun check(item: SeasonalRelayNotice): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal relay notice. */
data class SeasonalRelayNoticeValidator(var rules: MutableList<SeasonalRelayNoticeRule>) {
    fun validate(item: SeasonalRelayNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRelayNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRelayNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRelayNoticeValidator(): SeasonalRelayNoticeValidator {
    return SeasonalRelayNoticeValidator(
        mutableListOf<SeasonalRelayNoticeRule>(
            SeasonalRelayNoticeThresholdRangeRule(8.8, 3906.8),
            SeasonalRelayNoticeAllowedCodeRule(mutableListOf<String>("strict", "weighted", "weighted")),
            SeasonalRelayNoticeLabelLengthRule(80),
            SeasonalRelayNoticeFloorValueRangeRule(0.7, 2486.4),
        ),
    )
}
