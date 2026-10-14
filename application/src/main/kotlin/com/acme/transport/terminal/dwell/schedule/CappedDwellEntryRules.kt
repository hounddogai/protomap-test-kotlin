package com.acme.transport.terminal.dwell.schedule

/** A rule a capped dwell entry must pass. Check returns its problems, or none. */
interface CappedDwellEntryRule {
    fun check(item: CappedDwellEntry): MutableList<String>
}

data class CappedDwellEntryRequiredTitleRule(var message: String) : CappedDwellEntryRule {
    override fun check(item: CappedDwellEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellEntryCodePrefixRule(var prefix: String) : CappedDwellEntryRule {
    override fun check(item: CappedDwellEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellEntryCostRangeRule(var min: Double, var max: Double) : CappedDwellEntryRule {
    override fun check(item: CappedDwellEntry): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dwell entry. */
data class CappedDwellEntryValidator(var rules: MutableList<CappedDwellEntryRule>) {
    fun validate(item: CappedDwellEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDwellEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellEntryValidator(): CappedDwellEntryValidator {
    return CappedDwellEntryValidator(
        mutableListOf<CappedDwellEntryRule>(
            CappedDwellEntryRequiredTitleRule("capped dwell entry needs this field"),
            CappedDwellEntryCodePrefixRule("OPS"),
            CappedDwellEntryCostRangeRule(2.3, 3901.7),
        ),
    )
}
