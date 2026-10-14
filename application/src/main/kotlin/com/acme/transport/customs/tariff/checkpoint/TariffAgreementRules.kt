package com.acme.transport.customs.tariff.checkpoint

/** A rule a tariff agreement must pass. Check returns its problems, or none. */
interface TariffAgreementRule {
    fun check(item: TariffAgreement): MutableList<String>
}

data class TariffAgreementCostRangeRule(var min: Double, var max: Double) : TariffAgreementRule {
    override fun check(item: TariffAgreement): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffAgreementCodeLengthRule(var maxLength: Int) : TariffAgreementRule {
    override fun check(item: TariffAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffAgreementRequiredLabelRule(var message: String) : TariffAgreementRule {
    override fun check(item: TariffAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffAgreementPinnedGuardRule(var limit: Double) : TariffAgreementRule {
    override fun check(item: TariffAgreement): MutableList<String> {
        if (item.pinned && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class TariffAgreementCodePrefixRule(var prefix: String) : TariffAgreementRule {
    override fun check(item: TariffAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff agreement. */
data class TariffAgreementValidator(var rules: MutableList<TariffAgreementRule>) {
    fun validate(item: TariffAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffAgreementValidator(): TariffAgreementValidator {
    return TariffAgreementValidator(
        mutableListOf<TariffAgreementRule>(
            TariffAgreementCostRangeRule(3.7, 1454.1),
            TariffAgreementCodeLengthRule(16),
            TariffAgreementRequiredLabelRule("tariff agreement needs an entry"),
            TariffAgreementPinnedGuardRule(6.1),
            TariffAgreementCodePrefixRule("OPS"),
        ),
    )
}
