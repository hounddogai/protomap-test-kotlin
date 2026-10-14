package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.normalize

/** A rule a lenient redelivery snapshot must pass. Check returns its problems, or none. */
interface LenientRedeliverySnapshotRule {
    fun check(item: LenientRedeliverySnapshot): MutableList<String>
}

data class LenientRedeliverySnapshotRequiredReviewerRule(var message: String) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRedeliverySnapshotAllowedReviewerRule(
    var allowed: MutableList<String>,
) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LenientRedeliverySnapshotLabelLengthRule(var maxLength: Int) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRedeliverySnapshotCodeLengthRule(var maxLength: Int) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRedeliverySnapshotLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRedeliverySnapshotReviewerLengthRule(var maxLength: Int) : LenientRedeliverySnapshotRule {
    override fun check(item: LenientRedeliverySnapshot): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient redelivery snapshot. */
data class LenientRedeliverySnapshotValidator(var rules: MutableList<LenientRedeliverySnapshotRule>) {
    fun validate(item: LenientRedeliverySnapshot): MutableList<String> {
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

    fun requireValid(item: LenientRedeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRedeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRedeliverySnapshotValidator(): LenientRedeliverySnapshotValidator {
    return LenientRedeliverySnapshotValidator(
        mutableListOf<LenientRedeliverySnapshotRule>(
            LenientRedeliverySnapshotRequiredReviewerRule("lenient redelivery snapshot needs a value"),
            LenientRedeliverySnapshotAllowedReviewerRule(mutableListOf<String>("rolling", "fallback")),
            LenientRedeliverySnapshotLabelLengthRule(120),
            LenientRedeliverySnapshotCodeLengthRule(1000),
            LenientRedeliverySnapshotLoadKgRangeRule(2.1, 2413.9),
            LenientRedeliverySnapshotReviewerLengthRule(16),
        ),
    )
}
