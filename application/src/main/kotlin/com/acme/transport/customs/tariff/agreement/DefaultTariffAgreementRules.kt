package com.acme.transport.customs.tariff.agreement

/** A rule a default tariff agreement must pass. Check returns its problems, or none. */
interface DefaultTariffAgreementRule {
    fun check(item: DefaultTariffAgreement): MutableList<String>
}

data class DefaultTariffAgreementRequiredOwnerIdRule(var message: String) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffAgreementRequiredCountryRule(var message: String) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffAgreementCodePrefixRule(var prefix: String) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffAgreementVisibleGuardRule(var limit: Int) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        if (item.visible && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffAgreementBatchSizeRangeRule(var min: Int, var max: Int) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DefaultTariffAgreementCountryLengthRule(var maxLength: Int) : DefaultTariffAgreementRule {
    override fun check(item: DefaultTariffAgreement): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff agreement. */
data class DefaultTariffAgreementValidator(var rules: MutableList<DefaultTariffAgreementRule>) {
    fun validate(item: DefaultTariffAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTariffAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffAgreementValidator(): DefaultTariffAgreementValidator {
    return DefaultTariffAgreementValidator(
        mutableListOf<DefaultTariffAgreementRule>(
            DefaultTariffAgreementRequiredOwnerIdRule("default tariff agreement needs a value"),
            DefaultTariffAgreementRequiredCountryRule("default tariff agreement needs this field"),
            DefaultTariffAgreementCodePrefixRule("OPS"),
            DefaultTariffAgreementVisibleGuardRule(86400),
            DefaultTariffAgreementBatchSizeRangeRule(3600, 86400),
            DefaultTariffAgreementCountryLengthRule(1000),
        ),
    )
}
