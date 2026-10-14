package com.acme.transport.emissions.fuelburn.burnmilestone

/** A rule a pending fuel burn template must pass. Check returns its problems, or none. */
interface PendingFuelBurnTemplateRule {
    fun check(item: PendingFuelBurnTemplate): MutableList<String>
}

data class PendingFuelBurnTemplateRequiredTitleRule(var message: String) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnTemplateRequiredCountryRule(var message: String) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnTemplateRequiredCodeRule(var message: String) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnTemplateCodePrefixRule(var prefix: String) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnTemplateCountryLengthRule(var maxLength: Int) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnTemplateCodeLengthRule(var maxLength: Int) : PendingFuelBurnTemplateRule {
    override fun check(item: PendingFuelBurnTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel burn template. */
data class PendingFuelBurnTemplateValidator(var rules: MutableList<PendingFuelBurnTemplateRule>) {
    fun validate(item: PendingFuelBurnTemplate): MutableList<String> {
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

    fun requireValid(item: PendingFuelBurnTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelBurnTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelBurnTemplateValidator(): PendingFuelBurnTemplateValidator {
    return PendingFuelBurnTemplateValidator(
        mutableListOf<PendingFuelBurnTemplateRule>(
            PendingFuelBurnTemplateRequiredTitleRule("pending fuel burn template needs this field"),
            PendingFuelBurnTemplateRequiredCountryRule("pending fuel burn template needs this field"),
            PendingFuelBurnTemplateRequiredCodeRule("pending fuel burn template needs an entry"),
            PendingFuelBurnTemplateCodePrefixRule("OPS"),
            PendingFuelBurnTemplateCountryLengthRule(64),
            PendingFuelBurnTemplateCodeLengthRule(255),
        ),
    )
}
