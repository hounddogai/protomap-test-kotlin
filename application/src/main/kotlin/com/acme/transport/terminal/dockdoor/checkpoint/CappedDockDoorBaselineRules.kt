package com.acme.transport.terminal.dockdoor.checkpoint

/** A rule a capped dock door baseline must pass. Check returns its problems, or none. */
interface CappedDockDoorBaselineRule {
    fun check(item: CappedDockDoorBaseline): MutableList<String>
}

data class CappedDockDoorBaselineRequiredSummaryRule(var message: String) : CappedDockDoorBaselineRule {
    override fun check(item: CappedDockDoorBaseline): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorBaselineSummaryLengthRule(var maxLength: Int) : CappedDockDoorBaselineRule {
    override fun check(item: CappedDockDoorBaseline): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorBaselineRequiredTitleRule(var message: String) : CappedDockDoorBaselineRule {
    override fun check(item: CappedDockDoorBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorBaselineRequiredCurrencyRule(var message: String) : CappedDockDoorBaselineRule {
    override fun check(item: CappedDockDoorBaseline): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dock door baseline. */
data class CappedDockDoorBaselineValidator(var rules: MutableList<CappedDockDoorBaselineRule>) {
    fun validate(item: CappedDockDoorBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDockDoorBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDockDoorBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDockDoorBaselineValidator(): CappedDockDoorBaselineValidator {
    return CappedDockDoorBaselineValidator(
        mutableListOf<CappedDockDoorBaselineRule>(
            CappedDockDoorBaselineRequiredSummaryRule("capped dock door baseline needs this field"),
            CappedDockDoorBaselineSummaryLengthRule(255),
            CappedDockDoorBaselineRequiredTitleRule("capped dock door baseline needs an entry"),
            CappedDockDoorBaselineRequiredCurrencyRule("capped dock door baseline needs an entry"),
        ),
    )
}
