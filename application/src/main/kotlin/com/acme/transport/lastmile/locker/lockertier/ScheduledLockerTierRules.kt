package com.acme.transport.lastmile.locker.lockertier

/** A rule a scheduled locker tier must pass. Check returns its problems, or none. */
interface ScheduledLockerTierRule {
    fun check(item: ScheduledLockerTier): MutableList<String>
}

data class ScheduledLockerTierCategoryLengthRule(var maxLength: Int) : ScheduledLockerTierRule {
    override fun check(item: ScheduledLockerTier): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerTierRequiredCategoryRule(var message: String) : ScheduledLockerTierRule {
    override fun check(item: ScheduledLockerTier): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerTierExternalRefLengthRule(var maxLength: Int) : ScheduledLockerTierRule {
    override fun check(item: ScheduledLockerTier): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker tier. */
data class ScheduledLockerTierValidator(var rules: MutableList<ScheduledLockerTierRule>) {
    fun validate(item: ScheduledLockerTier): MutableList<String> {
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

    fun requireValid(item: ScheduledLockerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerTierValidator(): ScheduledLockerTierValidator {
    return ScheduledLockerTierValidator(
        mutableListOf<ScheduledLockerTierRule>(
            ScheduledLockerTierCategoryLengthRule(64),
            ScheduledLockerTierRequiredCategoryRule("scheduled locker tier needs a value"),
            ScheduledLockerTierExternalRefLengthRule(16),
        ),
    )
}
