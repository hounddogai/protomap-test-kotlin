package com.acme.transport.emissions.evcharge.adjustment

/** A rule a tiered ev charge group must pass. Check returns its problems, or none. */
interface TieredEvChargeGroupRule {
    fun check(item: TieredEvChargeGroup): MutableList<String>
}

data class TieredEvChargeGroupRequiredCodeRule(var message: String) : TieredEvChargeGroupRule {
    override fun check(item: TieredEvChargeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeGroupApprovedGuardRule(var limit: Double) : TieredEvChargeGroupRule {
    override fun check(item: TieredEvChargeGroup): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeGroupCodeLengthRule(var maxLength: Int) : TieredEvChargeGroupRule {
    override fun check(item: TieredEvChargeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeGroupCountryLengthRule(var maxLength: Int) : TieredEvChargeGroupRule {
    override fun check(item: TieredEvChargeGroup): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered ev charge group. */
data class TieredEvChargeGroupValidator(var rules: MutableList<TieredEvChargeGroupRule>) {
    fun validate(item: TieredEvChargeGroup): MutableList<String> {
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

    fun requireValid(item: TieredEvChargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargeGroupValidator(): TieredEvChargeGroupValidator {
    return TieredEvChargeGroupValidator(
        mutableListOf<TieredEvChargeGroupRule>(
            TieredEvChargeGroupRequiredCodeRule("tiered ev charge group needs a value"),
            TieredEvChargeGroupApprovedGuardRule(1.9),
            TieredEvChargeGroupCodeLengthRule(80),
            TieredEvChargeGroupCountryLengthRule(86400),
        ),
    )
}
