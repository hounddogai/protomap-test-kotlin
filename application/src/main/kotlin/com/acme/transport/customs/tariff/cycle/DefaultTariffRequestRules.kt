package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize

/** A rule a default tariff request must pass. Check returns its problems, or none. */
interface DefaultTariffRequestRule {
    fun check(item: DefaultTariffRequest): MutableList<String>
}

data class DefaultTariffRequestWeightRangeRule(var min: Double, var max: Double) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DefaultTariffRequestCodePrefixRule(var prefix: String) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffRequestNameLengthRule(var maxLength: Int) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffRequestLabelLengthRule(var maxLength: Int) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffRequestRequiredTimezoneRule(var message: String) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffRequestAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : DefaultTariffRequestRule {
    override fun check(item: DefaultTariffRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff request. */
data class DefaultTariffRequestValidator(var rules: MutableList<DefaultTariffRequestRule>) {
    fun validate(item: DefaultTariffRequest): MutableList<String> {
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

    fun requireValid(item: DefaultTariffRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffRequestValidator(): DefaultTariffRequestValidator {
    return DefaultTariffRequestValidator(
        mutableListOf<DefaultTariffRequestRule>(
            DefaultTariffRequestWeightRangeRule(4.2, 2560.2),
            DefaultTariffRequestCodePrefixRule("OPS"),
            DefaultTariffRequestNameLengthRule(86400),
            DefaultTariffRequestLabelLengthRule(80),
            DefaultTariffRequestRequiredTimezoneRule("default tariff request needs a value"),
            DefaultTariffRequestAllowedTimezoneRule(mutableListOf<String>("weighted")),
        ),
    )
}
