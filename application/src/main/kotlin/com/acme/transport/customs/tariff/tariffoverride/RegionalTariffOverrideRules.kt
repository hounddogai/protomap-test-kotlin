package com.acme.transport.customs.tariff.tariffoverride

/** A rule a regional tariff override must pass. Check returns its problems, or none. */
interface RegionalTariffOverrideRule {
    fun check(item: RegionalTariffOverride): MutableList<String>
}

data class RegionalTariffOverrideDescriptionLengthRule(var maxLength: Int) : RegionalTariffOverrideRule {
    override fun check(item: RegionalTariffOverride): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffOverrideCodeLengthRule(var maxLength: Int) : RegionalTariffOverrideRule {
    override fun check(item: RegionalTariffOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffOverrideRequiredDescriptionRule(var message: String) : RegionalTariffOverrideRule {
    override fun check(item: RegionalTariffOverride): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffOverrideRequiredCodeRule(var message: String) : RegionalTariffOverrideRule {
    override fun check(item: RegionalTariffOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tariff override. */
data class RegionalTariffOverrideValidator(var rules: MutableList<RegionalTariffOverrideRule>) {
    fun validate(item: RegionalTariffOverride): MutableList<String> {
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

    fun requireValid(item: RegionalTariffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTariffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTariffOverrideValidator(): RegionalTariffOverrideValidator {
    return RegionalTariffOverrideValidator(
        mutableListOf<RegionalTariffOverrideRule>(
            RegionalTariffOverrideDescriptionLengthRule(3600),
            RegionalTariffOverrideCodeLengthRule(120),
            RegionalTariffOverrideRequiredDescriptionRule("regional tariff override needs an entry"),
            RegionalTariffOverrideRequiredCodeRule("regional tariff override needs this field"),
        ),
    )
}
