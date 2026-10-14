package com.acme.transport.linehaul.tender.tier

/** A rule a tender entry must pass. Check returns its problems, or none. */
interface TenderEntryRule {
    fun check(item: TenderEntry): MutableList<String>
}

data class TenderEntryRequiredCodeRule(var message: String) : TenderEntryRule {
    override fun check(item: TenderEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderEntryRateRangeRule(var min: Double, var max: Double) : TenderEntryRule {
    override fun check(item: TenderEntry): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderEntryRequiredCountryRule(var message: String) : TenderEntryRule {
    override fun check(item: TenderEntry): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderEntryTimezoneLengthRule(var maxLength: Int) : TenderEntryRule {
    override fun check(item: TenderEntry): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderEntryCodeLengthRule(var maxLength: Int) : TenderEntryRule {
    override fun check(item: TenderEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender entry. */
data class TenderEntryValidator(var rules: MutableList<TenderEntryRule>) {
    fun validate(item: TenderEntry): MutableList<String> {
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

    fun requireValid(item: TenderEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderEntryValidator(): TenderEntryValidator {
    return TenderEntryValidator(
        mutableListOf<TenderEntryRule>(
            TenderEntryRequiredCodeRule("tender entry needs a value"),
            TenderEntryRateRangeRule(5.0, 138.3),
            TenderEntryRequiredCountryRule("tender entry needs a value"),
            TenderEntryTimezoneLengthRule(3600),
            TenderEntryCodeLengthRule(3600),
        ),
    )
}
