package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize

/** A rule a primary locker draft must pass. Check returns its problems, or none. */
interface PrimaryLockerDraftRule {
    fun check(item: PrimaryLockerDraft): MutableList<String>
}

data class PrimaryLockerDraftRequiredCodeRule(var message: String) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerDraftRequiredTrailerIdRule(var message: String) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerDraftCodeLengthRule(var maxLength: Int) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerDraftBatchSizeRangeRule(var min: Int, var max: Int) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class PrimaryLockerDraftGraceDaysRangeRule(var min: Int, var max: Int) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class PrimaryLockerDraftAllowedTrailerIdRule(var allowed: MutableList<String>) : PrimaryLockerDraftRule {
    override fun check(item: PrimaryLockerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary locker draft. */
data class PrimaryLockerDraftValidator(var rules: MutableList<PrimaryLockerDraftRule>) {
    fun validate(item: PrimaryLockerDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryLockerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLockerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLockerDraftValidator(): PrimaryLockerDraftValidator {
    return PrimaryLockerDraftValidator(
        mutableListOf<PrimaryLockerDraftRule>(
            PrimaryLockerDraftRequiredCodeRule("primary locker draft needs a value"),
            PrimaryLockerDraftRequiredTrailerIdRule("primary locker draft needs a value"),
            PrimaryLockerDraftCodeLengthRule(16),
            PrimaryLockerDraftBatchSizeRangeRule(32, 80),
            PrimaryLockerDraftGraceDaysRangeRule(120, 86400),
            PrimaryLockerDraftAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
