package com.acme.transport.emissions.evcharge.chargequota

/** A rule a seasonal ev charge quota must pass. Check returns its problems, or none. */
interface SeasonalEvChargeQuotaRule {
    fun check(item: SeasonalEvChargeQuota): MutableList<String>
}

data class SeasonalEvChargeQuotaNameLengthRule(var maxLength: Int) : SeasonalEvChargeQuotaRule {
    override fun check(item: SeasonalEvChargeQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeQuotaRequiredCodeRule(var message: String) : SeasonalEvChargeQuotaRule {
    override fun check(item: SeasonalEvChargeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeQuotaRequiredShiftCodeRule(var message: String) : SeasonalEvChargeQuotaRule {
    override fun check(item: SeasonalEvChargeQuota): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEvChargeQuotaLabelLengthRule(var maxLength: Int) : SeasonalEvChargeQuotaRule {
    override fun check(item: SeasonalEvChargeQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal ev charge quota. */
data class SeasonalEvChargeQuotaValidator(var rules: MutableList<SeasonalEvChargeQuotaRule>) {
    fun validate(item: SeasonalEvChargeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalEvChargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEvChargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEvChargeQuotaValidator(): SeasonalEvChargeQuotaValidator {
    return SeasonalEvChargeQuotaValidator(
        mutableListOf<SeasonalEvChargeQuotaRule>(
            SeasonalEvChargeQuotaNameLengthRule(255),
            SeasonalEvChargeQuotaRequiredCodeRule("seasonal ev charge quota needs an entry"),
            SeasonalEvChargeQuotaRequiredShiftCodeRule("seasonal ev charge quota needs an entry"),
            SeasonalEvChargeQuotaLabelLengthRule(86400),
        ),
    )
}
