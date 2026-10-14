package com.acme.transport.customs.tariff.tariffprofile

/** A rule a lenient tariff entry must pass. Check returns its problems, or none. */
interface LenientTariffEntryRule {
    fun check(item: LenientTariffEntry): MutableList<String>
}

data class LenientTariffEntryCodeLengthRule(var maxLength: Int) : LenientTariffEntryRule {
    override fun check(item: LenientTariffEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffEntryRatioRangeRule(var min: Double, var max: Double) : LenientTariffEntryRule {
    override fun check(item: LenientTariffEntry): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffEntryCodePrefixRule(var prefix: String) : LenientTariffEntryRule {
    override fun check(item: LenientTariffEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffEntryRequiredLaneRule(var message: String) : LenientTariffEntryRule {
    override fun check(item: LenientTariffEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tariff entry. */
data class LenientTariffEntryValidator(var rules: MutableList<LenientTariffEntryRule>) {
    fun validate(item: LenientTariffEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTariffEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTariffEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTariffEntryValidator(): LenientTariffEntryValidator {
    return LenientTariffEntryValidator(
        mutableListOf<LenientTariffEntryRule>(
            LenientTariffEntryCodeLengthRule(1000),
            LenientTariffEntryRatioRangeRule(0.6, 4922.4),
            LenientTariffEntryCodePrefixRule("ACM"),
            LenientTariffEntryRequiredLaneRule("lenient tariff entry needs this field"),
        ),
    )
}
