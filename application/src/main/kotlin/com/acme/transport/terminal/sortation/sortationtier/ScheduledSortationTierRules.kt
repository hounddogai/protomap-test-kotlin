package com.acme.transport.terminal.sortation.sortationtier

/** A rule a scheduled sortation tier must pass. Check returns its problems, or none. */
interface ScheduledSortationTierRule {
    fun check(item: ScheduledSortationTier): MutableList<String>
}

data class ScheduledSortationTierRequiredNameRule(var message: String) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationTierDockLengthRule(var maxLength: Int) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationTierScoreRangeRule(var min: Double, var max: Double) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ScheduledSortationTierCreatedAtRangeRule(var min: Int, var max: Int) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationTierRequiredDockRule(var message: String) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationTierBatchSizeRangeRule(var min: Int, var max: Int) : ScheduledSortationTierRule {
    override fun check(item: ScheduledSortationTier): MutableList<String> {
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

/** Runs every rule of a scheduled sortation tier. */
data class ScheduledSortationTierValidator(var rules: MutableList<ScheduledSortationTierRule>) {
    fun validate(item: ScheduledSortationTier): MutableList<String> {
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

    fun requireValid(item: ScheduledSortationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSortationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSortationTierValidator(): ScheduledSortationTierValidator {
    return ScheduledSortationTierValidator(
        mutableListOf<ScheduledSortationTierRule>(
            ScheduledSortationTierRequiredNameRule("scheduled sortation tier needs this field"),
            ScheduledSortationTierDockLengthRule(86400),
            ScheduledSortationTierScoreRangeRule(3.3, 4382.7),
            ScheduledSortationTierCreatedAtRangeRule(16, 64),
            ScheduledSortationTierRequiredDockRule("scheduled sortation tier needs an entry"),
            ScheduledSortationTierBatchSizeRangeRule(16, 120),
        ),
    )
}
