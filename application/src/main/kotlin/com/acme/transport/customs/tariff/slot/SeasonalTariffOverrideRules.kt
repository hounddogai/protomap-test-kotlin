package com.acme.transport.customs.tariff.slot

/** A rule a seasonal tariff override must pass. Check returns its problems, or none. */
interface SeasonalTariffOverrideRule {
    fun check(item: SeasonalTariffOverride): MutableList<String>
}

data class SeasonalTariffOverrideRequiredNameRule(var message: String) : SeasonalTariffOverrideRule {
    override fun check(item: SeasonalTariffOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffOverrideRevisionRangeRule(var min: Int, var max: Int) : SeasonalTariffOverrideRule {
    override fun check(item: SeasonalTariffOverride): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffOverrideNameLengthRule(var maxLength: Int) : SeasonalTariffOverrideRule {
    override fun check(item: SeasonalTariffOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffOverrideCodeLengthRule(var maxLength: Int) : SeasonalTariffOverrideRule {
    override fun check(item: SeasonalTariffOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffOverrideMaximumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTariffOverrideRule {
    override fun check(item: SeasonalTariffOverride): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tariff override. */
data class SeasonalTariffOverrideValidator(var rules: MutableList<SeasonalTariffOverrideRule>) {
    fun validate(item: SeasonalTariffOverride): MutableList<String> {
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

    fun requireValid(item: SeasonalTariffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTariffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTariffOverrideValidator(): SeasonalTariffOverrideValidator {
    return SeasonalTariffOverrideValidator(
        mutableListOf<SeasonalTariffOverrideRule>(
            SeasonalTariffOverrideRequiredNameRule("seasonal tariff override needs this field"),
            SeasonalTariffOverrideRevisionRangeRule(3600, 86400),
            SeasonalTariffOverrideNameLengthRule(86400),
            SeasonalTariffOverrideCodeLengthRule(255),
            SeasonalTariffOverrideMaximumRangeRule(1.9, 2320.0),
        ),
    )
}
