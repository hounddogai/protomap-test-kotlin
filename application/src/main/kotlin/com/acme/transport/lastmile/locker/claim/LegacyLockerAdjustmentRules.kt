package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize

/** A rule a legacy locker adjustment must pass. Check returns its problems, or none. */
interface LegacyLockerAdjustmentRule {
    fun check(item: LegacyLockerAdjustment): MutableList<String>
}

data class LegacyLockerAdjustmentRequiredCodeRule(var message: String) : LegacyLockerAdjustmentRule {
    override fun check(item: LegacyLockerAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAdjustmentSummaryLengthRule(var maxLength: Int) : LegacyLockerAdjustmentRule {
    override fun check(item: LegacyLockerAdjustment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAdjustmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : LegacyLockerAdjustmentRule {
    override fun check(item: LegacyLockerAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAdjustmentCodeLengthRule(var maxLength: Int) : LegacyLockerAdjustmentRule {
    override fun check(item: LegacyLockerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAdjustmentRequiredTitleRule(var message: String) : LegacyLockerAdjustmentRule {
    override fun check(item: LegacyLockerAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy locker adjustment. */
data class LegacyLockerAdjustmentValidator(var rules: MutableList<LegacyLockerAdjustmentRule>) {
    fun validate(item: LegacyLockerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLockerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLockerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLockerAdjustmentValidator(): LegacyLockerAdjustmentValidator {
    return LegacyLockerAdjustmentValidator(
        mutableListOf<LegacyLockerAdjustmentRule>(
            LegacyLockerAdjustmentRequiredCodeRule("legacy locker adjustment needs a value"),
            LegacyLockerAdjustmentSummaryLengthRule(255),
            LegacyLockerAdjustmentAllowedTitleRule(mutableListOf<String>()),
            LegacyLockerAdjustmentCodeLengthRule(3600),
            LegacyLockerAdjustmentRequiredTitleRule("legacy locker adjustment needs this field"),
        ),
    )
}
