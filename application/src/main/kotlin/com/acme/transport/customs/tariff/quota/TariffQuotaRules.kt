package com.acme.transport.customs.tariff.quota

/** A rule a tariff quota must pass. Check returns its problems, or none. */
interface TariffQuotaRule {
    fun check(item: TariffQuota): MutableList<String>
}

data class TariffQuotaFuelLitersRangeRule(var min: Double, var max: Double) : TariffQuotaRule {
    override fun check(item: TariffQuota): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class TariffQuotaNameLengthRule(var maxLength: Int) : TariffQuotaRule {
    override fun check(item: TariffQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffQuotaRequiredNameRule(var message: String) : TariffQuotaRule {
    override fun check(item: TariffQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff quota. */
data class TariffQuotaValidator(var rules: MutableList<TariffQuotaRule>) {
    fun validate(item: TariffQuota): MutableList<String> {
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

    fun requireValid(item: TariffQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffQuotaValidator(): TariffQuotaValidator {
    return TariffQuotaValidator(
        mutableListOf<TariffQuotaRule>(
            TariffQuotaFuelLitersRangeRule(3.9, 4178.3),
            TariffQuotaNameLengthRule(80),
            TariffQuotaRequiredNameRule("tariff quota needs this field"),
        ),
    )
}
