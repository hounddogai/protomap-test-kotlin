package com.acme.transport.customs.tariff.draft

/** A rule a tariff draft must pass. Check returns its problems, or none. */
interface TariffDraftRule {
    fun check(item: TariffDraft): MutableList<String>
}

data class TariffDraftRequiredCodeRule(var message: String) : TariffDraftRule {
    override fun check(item: TariffDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffDraftCeilingRangeRule(var min: Double, var max: Double) : TariffDraftRule {
    override fun check(item: TariffDraft): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffDraftCodePrefixRule(var prefix: String) : TariffDraftRule {
    override fun check(item: TariffDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffDraftNameLengthRule(var maxLength: Int) : TariffDraftRule {
    override fun check(item: TariffDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffDraftMinimumRangeRule(var min: Double, var max: Double) : TariffDraftRule {
    override fun check(item: TariffDraft): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tariff draft. */
data class TariffDraftValidator(var rules: MutableList<TariffDraftRule>) {
    fun validate(item: TariffDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffDraftValidator(): TariffDraftValidator {
    return TariffDraftValidator(
        mutableListOf<TariffDraftRule>(
            TariffDraftRequiredCodeRule("tariff draft needs this field"),
            TariffDraftCeilingRangeRule(1.2, 1020.4),
            TariffDraftCodePrefixRule("X"),
            TariffDraftNameLengthRule(86400),
            TariffDraftMinimumRangeRule(1.6, 2429.7),
        ),
    )
}
