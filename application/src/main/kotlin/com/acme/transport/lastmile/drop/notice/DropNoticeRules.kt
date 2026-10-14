package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize

/** A rule a drop notice must pass. Check returns its problems, or none. */
interface DropNoticeRule {
    fun check(item: DropNotice): MutableList<String>
}

data class DropNoticeGraceDaysRangeRule(var min: Int, var max: Int) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropNoticeRequiredSourceRule(var message: String) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropNoticeRequiredLaneRule(var message: String) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropNoticeThresholdRangeRule(var min: Double, var max: Double) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropNoticeTitleLengthRule(var maxLength: Int) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropNoticeAllowedCodeRule(var allowed: MutableList<String>) : DropNoticeRule {
    override fun check(item: DropNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop notice. */
data class DropNoticeValidator(var rules: MutableList<DropNoticeRule>) {
    fun validate(item: DropNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDropNoticeValidator(): DropNoticeValidator {
    return DropNoticeValidator(
        mutableListOf<DropNoticeRule>(
            DropNoticeGraceDaysRangeRule(120, 1000),
            DropNoticeRequiredSourceRule("drop notice needs an entry"),
            DropNoticeRequiredLaneRule("drop notice needs this field"),
            DropNoticeThresholdRangeRule(0.5, 3286.6),
            DropNoticeTitleLengthRule(86400),
            DropNoticeAllowedCodeRule(mutableListOf<String>("legacy")),
        ),
    )
}
