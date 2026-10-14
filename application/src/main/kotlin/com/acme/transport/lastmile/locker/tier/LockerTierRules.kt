package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize

/** A rule a locker tier must pass. Check returns its problems, or none. */
interface LockerTierRule {
    fun check(item: LockerTier): MutableList<String>
}

data class LockerTierRequiredTitleRule(var message: String) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerTierRequiredNotesRule(var message: String) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerTierRequiredCodeRule(var message: String) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerTierNotesLengthRule(var maxLength: Int) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerTierAllowedSummaryRule(var allowed: MutableList<String>) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LockerTierTitleLengthRule(var maxLength: Int) : LockerTierRule {
    override fun check(item: LockerTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker tier. */
data class LockerTierValidator(var rules: MutableList<LockerTierRule>) {
    fun validate(item: LockerTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerTierValidator(): LockerTierValidator {
    return LockerTierValidator(
        mutableListOf<LockerTierRule>(
            LockerTierRequiredTitleRule("locker tier needs this field"),
            LockerTierRequiredNotesRule("locker tier needs an entry"),
            LockerTierRequiredCodeRule("locker tier needs a value"),
            LockerTierNotesLengthRule(3600),
            LockerTierAllowedSummaryRule(mutableListOf<String>("tiered", "adaptive", "rolling")),
            LockerTierTitleLengthRule(120),
        ),
    )
}
