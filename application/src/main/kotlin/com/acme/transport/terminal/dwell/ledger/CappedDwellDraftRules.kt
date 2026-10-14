package com.acme.transport.terminal.dwell.ledger

/** A rule a capped dwell draft must pass. Check returns its problems, or none. */
interface CappedDwellDraftRule {
    fun check(item: CappedDwellDraft): MutableList<String>
}

data class CappedDwellDraftLocaleLengthRule(var maxLength: Int) : CappedDwellDraftRule {
    override fun check(item: CappedDwellDraft): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellDraftLimitRangeRule(var min: Int, var max: Int) : CappedDwellDraftRule {
    override fun check(item: CappedDwellDraft): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CappedDwellDraftStopCountRangeRule(var min: Int, var max: Int) : CappedDwellDraftRule {
    override fun check(item: CappedDwellDraft): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellDraftRequiredLocaleRule(var message: String) : CappedDwellDraftRule {
    override fun check(item: CappedDwellDraft): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dwell draft. */
data class CappedDwellDraftValidator(var rules: MutableList<CappedDwellDraftRule>) {
    fun validate(item: CappedDwellDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDwellDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellDraftValidator(): CappedDwellDraftValidator {
    return CappedDwellDraftValidator(
        mutableListOf<CappedDwellDraftRule>(
            CappedDwellDraftLocaleLengthRule(64),
            CappedDwellDraftLimitRangeRule(120, 3600),
            CappedDwellDraftStopCountRangeRule(80, 3600),
            CappedDwellDraftRequiredLocaleRule("capped dwell draft needs a value"),
        ),
    )
}
