package com.acme.transport.terminal.dwell.variance

/** A rule a fallback dwell notice must pass. Check returns its problems, or none. */
interface FallbackDwellNoticeRule {
    fun check(item: FallbackDwellNotice): MutableList<String>
}

data class FallbackDwellNoticeLabelLengthRule(var maxLength: Int) : FallbackDwellNoticeRule {
    override fun check(item: FallbackDwellNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellNoticeScoreRangeRule(var min: Double, var max: Double) : FallbackDwellNoticeRule {
    override fun check(item: FallbackDwellNotice): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellNoticeCodeLengthRule(var maxLength: Int) : FallbackDwellNoticeRule {
    override fun check(item: FallbackDwellNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell notice. */
data class FallbackDwellNoticeValidator(var rules: MutableList<FallbackDwellNoticeRule>) {
    fun validate(item: FallbackDwellNotice): MutableList<String> {
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

    fun requireValid(item: FallbackDwellNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellNoticeValidator(): FallbackDwellNoticeValidator {
    return FallbackDwellNoticeValidator(
        mutableListOf<FallbackDwellNoticeRule>(
            FallbackDwellNoticeLabelLengthRule(80),
            FallbackDwellNoticeScoreRangeRule(1.0, 378.2),
            FallbackDwellNoticeCodeLengthRule(86400),
        ),
    )
}
