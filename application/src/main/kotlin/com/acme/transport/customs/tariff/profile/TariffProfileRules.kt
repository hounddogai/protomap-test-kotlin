package com.acme.transport.customs.tariff.profile

/** A rule a tariff profile must pass. Check returns its problems, or none. */
interface TariffProfileRule {
    fun check(item: TariffProfile): MutableList<String>
}

data class TariffProfileQuantityRangeRule(var min: Int, var max: Int) : TariffProfileRule {
    override fun check(item: TariffProfile): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffProfileRequiredLabelRule(var message: String) : TariffProfileRule {
    override fun check(item: TariffProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffProfileCodeLengthRule(var maxLength: Int) : TariffProfileRule {
    override fun check(item: TariffProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffProfileLabelLengthRule(var maxLength: Int) : TariffProfileRule {
    override fun check(item: TariffProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffProfileWeightRangeRule(var min: Double, var max: Double) : TariffProfileRule {
    override fun check(item: TariffProfile): MutableList<String> {
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

/** Runs every rule of a tariff profile. */
data class TariffProfileValidator(var rules: MutableList<TariffProfileRule>) {
    fun validate(item: TariffProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffProfileValidator(): TariffProfileValidator {
    return TariffProfileValidator(
        mutableListOf<TariffProfileRule>(
            TariffProfileQuantityRangeRule(16, 3600),
            TariffProfileRequiredLabelRule("tariff profile needs a value"),
            TariffProfileCodeLengthRule(120),
            TariffProfileLabelLengthRule(16),
            TariffProfileWeightRangeRule(1.0, 2123.7),
        ),
    )
}
