package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.kit.text.normalize

/** A rule a default tariff target must pass. Check returns its problems, or none. */
interface DefaultTariffTargetRule {
    fun check(item: DefaultTariffTarget): MutableList<String>
}

data class DefaultTariffTargetRequiredTitleRule(var message: String) : DefaultTariffTargetRule {
    override fun check(item: DefaultTariffTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffTargetCodeLengthRule(var maxLength: Int) : DefaultTariffTargetRule {
    override fun check(item: DefaultTariffTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffTargetCodePrefixRule(var prefix: String) : DefaultTariffTargetRule {
    override fun check(item: DefaultTariffTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffTargetAllowedCarrierRule(var allowed: MutableList<String>) : DefaultTariffTargetRule {
    override fun check(item: DefaultTariffTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffTargetPriorityRangeRule(var min: Int, var max: Int) : DefaultTariffTargetRule {
    override fun check(item: DefaultTariffTarget): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff target. */
data class DefaultTariffTargetValidator(var rules: MutableList<DefaultTariffTargetRule>) {
    fun validate(item: DefaultTariffTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTariffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffTargetValidator(): DefaultTariffTargetValidator {
    return DefaultTariffTargetValidator(
        mutableListOf<DefaultTariffTargetRule>(
            DefaultTariffTargetRequiredTitleRule("default tariff target needs this field"),
            DefaultTariffTargetCodeLengthRule(3600),
            DefaultTariffTargetCodePrefixRule("Q"),
            DefaultTariffTargetAllowedCarrierRule(mutableListOf<String>("tiered", "capped", "weighted")),
            DefaultTariffTargetPriorityRangeRule(120, 255),
        ),
    )
}
