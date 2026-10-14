package com.acme.transport.freightpay.ratecard.target

/** A rule a tiered rate card manifest must pass. Check returns its problems, or none. */
interface TieredRateCardManifestRule {
    fun check(item: TieredRateCardManifest): MutableList<String>
}

data class TieredRateCardManifestCodePrefixRule(var prefix: String) : TieredRateCardManifestRule {
    override fun check(item: TieredRateCardManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardManifestRequiredCodeRule(var message: String) : TieredRateCardManifestRule {
    override fun check(item: TieredRateCardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardManifestPriorityRangeRule(var min: Int, var max: Int) : TieredRateCardManifestRule {
    override fun check(item: TieredRateCardManifest): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rate card manifest. */
data class TieredRateCardManifestValidator(var rules: MutableList<TieredRateCardManifestRule>) {
    fun validate(item: TieredRateCardManifest): MutableList<String> {
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

    fun requireValid(item: TieredRateCardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRateCardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRateCardManifestValidator(): TieredRateCardManifestValidator {
    return TieredRateCardManifestValidator(
        mutableListOf<TieredRateCardManifestRule>(
            TieredRateCardManifestCodePrefixRule("REF"),
            TieredRateCardManifestRequiredCodeRule("tiered rate card manifest needs an entry"),
            TieredRateCardManifestPriorityRangeRule(1000, 3600),
        ),
    )
}
