package com.acme.transport.lastmile.parcel.parcelbaseline

/** A rule a capped parcel quota must pass. Check returns its problems, or none. */
interface CappedParcelQuotaRule {
    fun check(item: CappedParcelQuota): MutableList<String>
}

data class CappedParcelQuotaUnitLengthRule(var maxLength: Int) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelQuotaCodePrefixRule(var prefix: String) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelQuotaRequiredCodeRule(var message: String) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelQuotaWeightRangeRule(var min: Double, var max: Double) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelQuotaCodeLengthRule(var maxLength: Int) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelQuotaRequiredTimezoneRule(var message: String) : CappedParcelQuotaRule {
    override fun check(item: CappedParcelQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped parcel quota. */
data class CappedParcelQuotaValidator(var rules: MutableList<CappedParcelQuotaRule>) {
    fun validate(item: CappedParcelQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedParcelQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedParcelQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedParcelQuotaValidator(): CappedParcelQuotaValidator {
    return CappedParcelQuotaValidator(
        mutableListOf<CappedParcelQuotaRule>(
            CappedParcelQuotaUnitLengthRule(255),
            CappedParcelQuotaCodePrefixRule("ACM"),
            CappedParcelQuotaRequiredCodeRule("capped parcel quota needs this field"),
            CappedParcelQuotaWeightRangeRule(9.3, 725.7),
            CappedParcelQuotaCodeLengthRule(120),
            CappedParcelQuotaRequiredTimezoneRule("capped parcel quota needs an entry"),
        ),
    )
}
