package com.acme.transport.emissions.evcharge.agreement

/** A rule a tiered ev charge quota must pass. Check returns its problems, or none. */
interface TieredEvChargeQuotaRule {
    fun check(item: TieredEvChargeQuota): MutableList<String>
}

data class TieredEvChargeQuotaNameLengthRule(var maxLength: Int) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeQuotaPriorityRangeRule(var min: Int, var max: Int) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class TieredEvChargeQuotaRequiredNameRule(var message: String) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeQuotaRequiredCodeRule(var message: String) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeQuotaRequiredCarrierRule(var message: String) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeQuotaCeilingRangeRule(var min: Double, var max: Double) : TieredEvChargeQuotaRule {
    override fun check(item: TieredEvChargeQuota): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered ev charge quota. */
data class TieredEvChargeQuotaValidator(var rules: MutableList<TieredEvChargeQuotaRule>) {
    fun validate(item: TieredEvChargeQuota): MutableList<String> {
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

    fun requireValid(item: TieredEvChargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargeQuotaValidator(): TieredEvChargeQuotaValidator {
    return TieredEvChargeQuotaValidator(
        mutableListOf<TieredEvChargeQuotaRule>(
            TieredEvChargeQuotaNameLengthRule(86400),
            TieredEvChargeQuotaPriorityRangeRule(1000, 3600),
            TieredEvChargeQuotaRequiredNameRule("tiered ev charge quota needs this field"),
            TieredEvChargeQuotaRequiredCodeRule("tiered ev charge quota needs this field"),
            TieredEvChargeQuotaRequiredCarrierRule("tiered ev charge quota needs a value"),
            TieredEvChargeQuotaCeilingRangeRule(7.7, 690.4),
        ),
    )
}
