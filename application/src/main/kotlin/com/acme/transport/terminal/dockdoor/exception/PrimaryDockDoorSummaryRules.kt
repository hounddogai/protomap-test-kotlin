package com.acme.transport.terminal.dockdoor.exception

/** A rule a primary dock door summary must pass. Check returns its problems, or none. */
interface PrimaryDockDoorSummaryRule {
    fun check(item: PrimaryDockDoorSummary): MutableList<String>
}

data class PrimaryDockDoorSummaryStartsAtRangeRule(var min: Int, var max: Int) : PrimaryDockDoorSummaryRule {
    override fun check(item: PrimaryDockDoorSummary): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class PrimaryDockDoorSummaryRatioRangeRule(var min: Double, var max: Double) : PrimaryDockDoorSummaryRule {
    override fun check(item: PrimaryDockDoorSummary): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PrimaryDockDoorSummaryRequiredLocaleRule(var message: String) : PrimaryDockDoorSummaryRule {
    override fun check(item: PrimaryDockDoorSummary): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorSummaryRequiredTitleRule(var message: String) : PrimaryDockDoorSummaryRule {
    override fun check(item: PrimaryDockDoorSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorSummaryLocaleLengthRule(var maxLength: Int) : PrimaryDockDoorSummaryRule {
    override fun check(item: PrimaryDockDoorSummary): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dock door summary. */
data class PrimaryDockDoorSummaryValidator(var rules: MutableList<PrimaryDockDoorSummaryRule>) {
    fun validate(item: PrimaryDockDoorSummary): MutableList<String> {
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

    fun requireValid(item: PrimaryDockDoorSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDockDoorSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDockDoorSummaryValidator(): PrimaryDockDoorSummaryValidator {
    return PrimaryDockDoorSummaryValidator(
        mutableListOf<PrimaryDockDoorSummaryRule>(
            PrimaryDockDoorSummaryStartsAtRangeRule(16, 255),
            PrimaryDockDoorSummaryRatioRangeRule(4.4, 1247.1),
            PrimaryDockDoorSummaryRequiredLocaleRule("primary dock door summary needs an entry"),
            PrimaryDockDoorSummaryRequiredTitleRule("primary dock door summary needs an entry"),
            PrimaryDockDoorSummaryLocaleLengthRule(80),
        ),
    )
}
