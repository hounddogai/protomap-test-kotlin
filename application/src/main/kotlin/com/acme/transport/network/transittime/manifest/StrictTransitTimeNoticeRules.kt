package com.acme.transport.network.transittime.manifest

/** A rule a strict transit time notice must pass. Check returns its problems, or none. */
interface StrictTransitTimeNoticeRule {
    fun check(item: StrictTransitTimeNotice): MutableList<String>
}

data class StrictTransitTimeNoticeRequiredCodeRule(var message: String) : StrictTransitTimeNoticeRule {
    override fun check(item: StrictTransitTimeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeNoticeRequiredTitleRule(var message: String) : StrictTransitTimeNoticeRule {
    override fun check(item: StrictTransitTimeNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeNoticeMaximumRangeRule(
    var min: Double,
    var max: Double,
) : StrictTransitTimeNoticeRule {
    override fun check(item: StrictTransitTimeNotice): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict transit time notice. */
data class StrictTransitTimeNoticeValidator(var rules: MutableList<StrictTransitTimeNoticeRule>) {
    fun validate(item: StrictTransitTimeNotice): MutableList<String> {
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

    fun requireValid(item: StrictTransitTimeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTransitTimeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTransitTimeNoticeValidator(): StrictTransitTimeNoticeValidator {
    return StrictTransitTimeNoticeValidator(
        mutableListOf<StrictTransitTimeNoticeRule>(
            StrictTransitTimeNoticeRequiredCodeRule("strict transit time notice needs this field"),
            StrictTransitTimeNoticeRequiredTitleRule("strict transit time notice needs an entry"),
            StrictTransitTimeNoticeMaximumRangeRule(3.1, 3363.8),
        ),
    )
}
