package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.normalize

/** A rule a legacy redelivery variance must pass. Check returns its problems, or none. */
interface LegacyRedeliveryVarianceRule {
    fun check(item: LegacyRedeliveryVariance): MutableList<String>
}

data class LegacyRedeliveryVarianceNotesLengthRule(var maxLength: Int) : LegacyRedeliveryVarianceRule {
    override fun check(item: LegacyRedeliveryVariance): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRedeliveryVarianceAllowedNotesRule(
    var allowed: MutableList<String>,
) : LegacyRedeliveryVarianceRule {
    override fun check(item: LegacyRedeliveryVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRedeliveryVarianceRequiredNameRule(var message: String) : LegacyRedeliveryVarianceRule {
    override fun check(item: LegacyRedeliveryVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRedeliveryVarianceNameLengthRule(var maxLength: Int) : LegacyRedeliveryVarianceRule {
    override fun check(item: LegacyRedeliveryVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy redelivery variance. */
data class LegacyRedeliveryVarianceValidator(var rules: MutableList<LegacyRedeliveryVarianceRule>) {
    fun validate(item: LegacyRedeliveryVariance): MutableList<String> {
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

    fun requireValid(item: LegacyRedeliveryVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRedeliveryVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRedeliveryVarianceValidator(): LegacyRedeliveryVarianceValidator {
    return LegacyRedeliveryVarianceValidator(
        mutableListOf<LegacyRedeliveryVarianceRule>(
            LegacyRedeliveryVarianceNotesLengthRule(1000),
            LegacyRedeliveryVarianceAllowedNotesRule(mutableListOf<String>("rolling")),
            LegacyRedeliveryVarianceRequiredNameRule("legacy redelivery variance needs a value"),
            LegacyRedeliveryVarianceNameLengthRule(16),
        ),
    )
}
