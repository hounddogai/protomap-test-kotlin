package com.acme.transport.terminal.dwell.bundle

/** A rule a adaptive dwell entry must pass. Check returns its problems, or none. */
interface AdaptiveDwellEntryRule {
    fun check(item: AdaptiveDwellEntry): MutableList<String>
}

data class AdaptiveDwellEntryStartsAtRangeRule(var min: Int, var max: Int) : AdaptiveDwellEntryRule {
    override fun check(item: AdaptiveDwellEntry): MutableList<String> {
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

data class AdaptiveDwellEntryRequiredNameRule(var message: String) : AdaptiveDwellEntryRule {
    override fun check(item: AdaptiveDwellEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellEntryRequiredExternalRefRule(var message: String) : AdaptiveDwellEntryRule {
    override fun check(item: AdaptiveDwellEntry): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dwell entry. */
data class AdaptiveDwellEntryValidator(var rules: MutableList<AdaptiveDwellEntryRule>) {
    fun validate(item: AdaptiveDwellEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDwellEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDwellEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDwellEntryValidator(): AdaptiveDwellEntryValidator {
    return AdaptiveDwellEntryValidator(
        mutableListOf<AdaptiveDwellEntryRule>(
            AdaptiveDwellEntryStartsAtRangeRule(80, 80),
            AdaptiveDwellEntryRequiredNameRule("adaptive dwell entry needs this field"),
            AdaptiveDwellEntryRequiredExternalRefRule("adaptive dwell entry needs a value"),
        ),
    )
}
