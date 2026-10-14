package com.acme.transport.customs.tariff.profile

/** A rule a tariff baseline must pass. Check returns its problems, or none. */
interface TariffBaselineRule {
    fun check(item: TariffBaseline): MutableList<String>
}

data class TariffBaselineRatioRangeRule(var min: Double, var max: Double) : TariffBaselineRule {
    override fun check(item: TariffBaseline): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffBaselineCodeLengthRule(var maxLength: Int) : TariffBaselineRule {
    override fun check(item: TariffBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffBaselineCodePrefixRule(var prefix: String) : TariffBaselineRule {
    override fun check(item: TariffBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffBaselineMaximumRangeRule(var min: Double, var max: Double) : TariffBaselineRule {
    override fun check(item: TariffBaseline): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class TariffBaselineRequiredNotesRule(var message: String) : TariffBaselineRule {
    override fun check(item: TariffBaseline): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff baseline. */
data class TariffBaselineValidator(var rules: MutableList<TariffBaselineRule>) {
    fun validate(item: TariffBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffBaselineValidator(): TariffBaselineValidator {
    return TariffBaselineValidator(
        mutableListOf<TariffBaselineRule>(
            TariffBaselineRatioRangeRule(1.6, 444.7),
            TariffBaselineCodeLengthRule(32),
            TariffBaselineCodePrefixRule("OPS"),
            TariffBaselineMaximumRangeRule(4.8, 4603.9),
            TariffBaselineRequiredNotesRule("tariff baseline needs an entry"),
        ),
    )
}
