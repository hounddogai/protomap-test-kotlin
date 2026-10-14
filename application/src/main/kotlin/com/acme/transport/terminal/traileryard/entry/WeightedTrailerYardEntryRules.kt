package com.acme.transport.terminal.traileryard.entry

/** A rule a weighted trailer yard entry must pass. Check returns its problems, or none. */
interface WeightedTrailerYardEntryRule {
    fun check(item: WeightedTrailerYardEntry): MutableList<String>
}

data class WeightedTrailerYardEntryCurrencyLengthRule(var maxLength: Int) : WeightedTrailerYardEntryRule {
    override fun check(item: WeightedTrailerYardEntry): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardEntryOwnerIdLengthRule(var maxLength: Int) : WeightedTrailerYardEntryRule {
    override fun check(item: WeightedTrailerYardEntry): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardEntryCodeLengthRule(var maxLength: Int) : WeightedTrailerYardEntryRule {
    override fun check(item: WeightedTrailerYardEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted trailer yard entry. */
data class WeightedTrailerYardEntryValidator(var rules: MutableList<WeightedTrailerYardEntryRule>) {
    fun validate(item: WeightedTrailerYardEntry): MutableList<String> {
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

    fun requireValid(item: WeightedTrailerYardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerYardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerYardEntryValidator(): WeightedTrailerYardEntryValidator {
    return WeightedTrailerYardEntryValidator(
        mutableListOf<WeightedTrailerYardEntryRule>(
            WeightedTrailerYardEntryCurrencyLengthRule(255),
            WeightedTrailerYardEntryOwnerIdLengthRule(3600),
            WeightedTrailerYardEntryCodeLengthRule(255),
        ),
    )
}
