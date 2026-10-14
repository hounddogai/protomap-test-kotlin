package com.acme.transport.customs.tariff.plan

/** A rule a fallback tariff baseline must pass. Check returns its problems, or none. */
interface FallbackTariffBaselineRule {
    fun check(item: FallbackTariffBaseline): MutableList<String>
}

data class FallbackTariffBaselineWindowDaysRangeRule(var min: Int, var max: Int) : FallbackTariffBaselineRule {
    override fun check(item: FallbackTariffBaseline): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffBaselineRequiredCodeRule(var message: String) : FallbackTariffBaselineRule {
    override fun check(item: FallbackTariffBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffBaselineDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTariffBaselineRule {
    override fun check(item: FallbackTariffBaseline): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffBaselineRequiredNameRule(var message: String) : FallbackTariffBaselineRule {
    override fun check(item: FallbackTariffBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffBaselineMaximumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTariffBaselineRule {
    override fun check(item: FallbackTariffBaseline): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tariff baseline. */
data class FallbackTariffBaselineValidator(var rules: MutableList<FallbackTariffBaselineRule>) {
    fun validate(item: FallbackTariffBaseline): MutableList<String> {
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

    fun requireValid(item: FallbackTariffBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTariffBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTariffBaselineValidator(): FallbackTariffBaselineValidator {
    return FallbackTariffBaselineValidator(
        mutableListOf<FallbackTariffBaselineRule>(
            FallbackTariffBaselineWindowDaysRangeRule(80, 255),
            FallbackTariffBaselineRequiredCodeRule("fallback tariff baseline needs this field"),
            FallbackTariffBaselineDurationMinutesRangeRule(32, 3600),
            FallbackTariffBaselineRequiredNameRule("fallback tariff baseline needs this field"),
            FallbackTariffBaselineMaximumRangeRule(9.2, 778.9),
        ),
    )
}
