package com.acme.transport.terminal.sortation.draft

/** A rule a sortation draft must pass. Check returns its problems, or none. */
interface SortationDraftRule {
    fun check(item: SortationDraft): MutableList<String>
}

data class SortationDraftPhoneLengthRule(var maxLength: Int) : SortationDraftRule {
    override fun check(item: SortationDraft): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationDraftRevisionRangeRule(var min: Int, var max: Int) : SortationDraftRule {
    override fun check(item: SortationDraft): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class SortationDraftRateRangeRule(var min: Double, var max: Double) : SortationDraftRule {
    override fun check(item: SortationDraft): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationDraftRequiredCountryRule(var message: String) : SortationDraftRule {
    override fun check(item: SortationDraft): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation draft. */
data class SortationDraftValidator(var rules: MutableList<SortationDraftRule>) {
    fun validate(item: SortationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationDraftValidator(): SortationDraftValidator {
    return SortationDraftValidator(
        mutableListOf<SortationDraftRule>(
            SortationDraftPhoneLengthRule(86400),
            SortationDraftRevisionRangeRule(16, 64),
            SortationDraftRateRangeRule(7.7, 2604.0),
            SortationDraftRequiredCountryRule("sortation draft needs an entry"),
        ),
    )
}
