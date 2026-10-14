package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.text.normalize

/** A rule a tariff entry must pass. Check returns its problems, or none. */
interface TariffEntryRule {
    fun check(item: TariffEntry): MutableList<String>
}

data class TariffEntryFloorValueRangeRule(var min: Double, var max: Double) : TariffEntryRule {
    override fun check(item: TariffEntry): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffEntryRequiredCodeRule(var message: String) : TariffEntryRule {
    override fun check(item: TariffEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffEntryCodePrefixRule(var prefix: String) : TariffEntryRule {
    override fun check(item: TariffEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffEntryAllowedCodeRule(var allowed: MutableList<String>) : TariffEntryRule {
    override fun check(item: TariffEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff entry. */
data class TariffEntryValidator(var rules: MutableList<TariffEntryRule>) {
    fun validate(item: TariffEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffEntryValidator(): TariffEntryValidator {
    return TariffEntryValidator(
        mutableListOf<TariffEntryRule>(
            TariffEntryFloorValueRangeRule(0.3, 939.1),
            TariffEntryRequiredCodeRule("tariff entry needs an entry"),
            TariffEntryCodePrefixRule("X"),
            TariffEntryAllowedCodeRule(mutableListOf<String>("capped", "global", "seasonal")),
        ),
    )
}
