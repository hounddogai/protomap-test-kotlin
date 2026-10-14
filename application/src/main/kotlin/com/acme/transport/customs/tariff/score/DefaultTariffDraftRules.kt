package com.acme.transport.customs.tariff.score

/** A rule a default tariff draft must pass. Check returns its problems, or none. */
interface DefaultTariffDraftRule {
    fun check(item: DefaultTariffDraft): MutableList<String>
}

data class DefaultTariffDraftShiftCodeLengthRule(var maxLength: Int) : DefaultTariffDraftRule {
    override fun check(item: DefaultTariffDraft): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffDraftRequiredTitleRule(var message: String) : DefaultTariffDraftRule {
    override fun check(item: DefaultTariffDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffDraftDistanceKmRangeRule(var min: Double, var max: Double) : DefaultTariffDraftRule {
    override fun check(item: DefaultTariffDraft): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class DefaultTariffDraftRequiredCodeRule(var message: String) : DefaultTariffDraftRule {
    override fun check(item: DefaultTariffDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffDraftRequiredContactEmailRule(var message: String) : DefaultTariffDraftRule {
    override fun check(item: DefaultTariffDraft): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff draft. */
data class DefaultTariffDraftValidator(var rules: MutableList<DefaultTariffDraftRule>) {
    fun validate(item: DefaultTariffDraft): MutableList<String> {
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

    fun requireValid(item: DefaultTariffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffDraftValidator(): DefaultTariffDraftValidator {
    return DefaultTariffDraftValidator(
        mutableListOf<DefaultTariffDraftRule>(
            DefaultTariffDraftShiftCodeLengthRule(1000),
            DefaultTariffDraftRequiredTitleRule("default tariff draft needs an entry"),
            DefaultTariffDraftDistanceKmRangeRule(3.0, 369.2),
            DefaultTariffDraftRequiredCodeRule("default tariff draft needs an entry"),
            DefaultTariffDraftRequiredContactEmailRule("default tariff draft needs this field"),
        ),
    )
}
