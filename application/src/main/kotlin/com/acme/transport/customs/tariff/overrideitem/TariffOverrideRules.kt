package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a tariff override must pass. Check returns its problems, or none. */
interface TariffOverrideRule {
    fun check(item: TariffOverride): MutableList<String>
}

data class TariffOverrideRequiredTitleRule(var message: String) : TariffOverrideRule {
    override fun check(item: TariffOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffOverrideRequiredCurrencyRule(var message: String) : TariffOverrideRule {
    override fun check(item: TariffOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffOverrideRequiredCodeRule(var message: String) : TariffOverrideRule {
    override fun check(item: TariffOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffOverrideAllowedLocaleRule(var allowed: MutableList<String>) : TariffOverrideRule {
    override fun check(item: TariffOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TariffOverrideTitleLengthRule(var maxLength: Int) : TariffOverrideRule {
    override fun check(item: TariffOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff override. */
data class TariffOverrideValidator(var rules: MutableList<TariffOverrideRule>) {
    fun validate(item: TariffOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffOverrideValidator(): TariffOverrideValidator {
    return TariffOverrideValidator(
        mutableListOf<TariffOverrideRule>(
            TariffOverrideRequiredTitleRule("tariff override needs an entry"),
            TariffOverrideRequiredCurrencyRule("tariff override needs a value"),
            TariffOverrideRequiredCodeRule("tariff override needs an entry"),
            TariffOverrideAllowedLocaleRule(mutableListOf<String>()),
            TariffOverrideTitleLengthRule(80),
        ),
    )
}
