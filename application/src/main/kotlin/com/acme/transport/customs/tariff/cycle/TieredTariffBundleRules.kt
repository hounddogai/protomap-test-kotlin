package com.acme.transport.customs.tariff.cycle

/** A rule a tiered tariff bundle must pass. Check returns its problems, or none. */
interface TieredTariffBundleRule {
    fun check(item: TieredTariffBundle): MutableList<String>
}

data class TieredTariffBundleRequiredCountryRule(var message: String) : TieredTariffBundleRule {
    override fun check(item: TieredTariffBundle): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffBundleEtaMinutesRangeRule(var min: Int, var max: Int) : TieredTariffBundleRule {
    override fun check(item: TieredTariffBundle): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffBundleCodeLengthRule(var maxLength: Int) : TieredTariffBundleRule {
    override fun check(item: TieredTariffBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tariff bundle. */
data class TieredTariffBundleValidator(var rules: MutableList<TieredTariffBundleRule>) {
    fun validate(item: TieredTariffBundle): MutableList<String> {
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

    fun requireValid(item: TieredTariffBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTariffBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTariffBundleValidator(): TieredTariffBundleValidator {
    return TieredTariffBundleValidator(
        mutableListOf<TieredTariffBundleRule>(
            TieredTariffBundleRequiredCountryRule("tiered tariff bundle needs an entry"),
            TieredTariffBundleEtaMinutesRangeRule(120, 3600),
            TieredTariffBundleCodeLengthRule(64),
        ),
    )
}
