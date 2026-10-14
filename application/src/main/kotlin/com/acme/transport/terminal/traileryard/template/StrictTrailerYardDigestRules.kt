package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize

/** A rule a strict trailer yard digest must pass. Check returns its problems, or none. */
interface StrictTrailerYardDigestRule {
    fun check(item: StrictTrailerYardDigest): MutableList<String>
}

data class StrictTrailerYardDigestBatchSizeRangeRule(var min: Int, var max: Int) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardDigestRequiredSummaryRule(var message: String) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardDigestAllowedNotesRule(
    var allowed: MutableList<String>,
) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is too long")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardDigestCodeLengthRule(var maxLength: Int) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardDigestNotesLengthRule(var maxLength: Int) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardDigestAttemptsRangeRule(var min: Int, var max: Int) : StrictTrailerYardDigestRule {
    override fun check(item: StrictTrailerYardDigest): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict trailer yard digest. */
data class StrictTrailerYardDigestValidator(var rules: MutableList<StrictTrailerYardDigestRule>) {
    fun validate(item: StrictTrailerYardDigest): MutableList<String> {
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

    fun requireValid(item: StrictTrailerYardDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerYardDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerYardDigestValidator(): StrictTrailerYardDigestValidator {
    return StrictTrailerYardDigestValidator(
        mutableListOf<StrictTrailerYardDigestRule>(
            StrictTrailerYardDigestBatchSizeRangeRule(64, 120),
            StrictTrailerYardDigestRequiredSummaryRule("strict trailer yard digest needs this field"),
            StrictTrailerYardDigestAllowedNotesRule(mutableListOf<String>("rolling")),
            StrictTrailerYardDigestCodeLengthRule(3600),
            StrictTrailerYardDigestNotesLengthRule(1000),
            StrictTrailerYardDigestAttemptsRangeRule(80, 86400),
        ),
    )
}
