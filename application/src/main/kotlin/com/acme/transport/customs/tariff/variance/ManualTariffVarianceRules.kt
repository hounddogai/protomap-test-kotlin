package com.acme.transport.customs.tariff.variance

/** A rule a manual tariff variance must pass. Check returns its problems, or none. */
interface ManualTariffVarianceRule {
    fun check(item: ManualTariffVariance): MutableList<String>
}

data class ManualTariffVarianceRequiredNameRule(var message: String) : ManualTariffVarianceRule {
    override fun check(item: ManualTariffVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffVarianceActiveGuardRule(var limit: Int) : ManualTariffVarianceRule {
    override fun check(item: ManualTariffVariance): MutableList<String> {
        if (item.active && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffVarianceNameLengthRule(var maxLength: Int) : ManualTariffVarianceRule {
    override fun check(item: ManualTariffVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffVarianceCodeLengthRule(var maxLength: Int) : ManualTariffVarianceRule {
    override fun check(item: ManualTariffVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tariff variance. */
data class ManualTariffVarianceValidator(var rules: MutableList<ManualTariffVarianceRule>) {
    fun validate(item: ManualTariffVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTariffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTariffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTariffVarianceValidator(): ManualTariffVarianceValidator {
    return ManualTariffVarianceValidator(
        mutableListOf<ManualTariffVarianceRule>(
            ManualTariffVarianceRequiredNameRule("manual tariff variance needs this field"),
            ManualTariffVarianceActiveGuardRule(16),
            ManualTariffVarianceNameLengthRule(120),
            ManualTariffVarianceCodeLengthRule(64),
        ),
    )
}
