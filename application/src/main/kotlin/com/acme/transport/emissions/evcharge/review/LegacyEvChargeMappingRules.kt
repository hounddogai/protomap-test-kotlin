package com.acme.transport.emissions.evcharge.review

/** A rule a legacy ev charge mapping must pass. Check returns its problems, or none. */
interface LegacyEvChargeMappingRule {
    fun check(item: LegacyEvChargeMapping): MutableList<String>
}

data class LegacyEvChargeMappingCeilingRangeRule(var min: Double, var max: Double) : LegacyEvChargeMappingRule {
    override fun check(item: LegacyEvChargeMapping): MutableList<String> {
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

data class LegacyEvChargeMappingNameLengthRule(var maxLength: Int) : LegacyEvChargeMappingRule {
    override fun check(item: LegacyEvChargeMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeMappingRequiredCodeRule(var message: String) : LegacyEvChargeMappingRule {
    override fun check(item: LegacyEvChargeMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeMappingCodeLengthRule(var maxLength: Int) : LegacyEvChargeMappingRule {
    override fun check(item: LegacyEvChargeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeMappingCostRangeRule(var min: Double, var max: Double) : LegacyEvChargeMappingRule {
    override fun check(item: LegacyEvChargeMapping): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy ev charge mapping. */
data class LegacyEvChargeMappingValidator(var rules: MutableList<LegacyEvChargeMappingRule>) {
    fun validate(item: LegacyEvChargeMapping): MutableList<String> {
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

    fun requireValid(item: LegacyEvChargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyEvChargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyEvChargeMappingValidator(): LegacyEvChargeMappingValidator {
    return LegacyEvChargeMappingValidator(
        mutableListOf<LegacyEvChargeMappingRule>(
            LegacyEvChargeMappingCeilingRangeRule(9.4, 467.6),
            LegacyEvChargeMappingNameLengthRule(80),
            LegacyEvChargeMappingRequiredCodeRule("legacy ev charge mapping needs an entry"),
            LegacyEvChargeMappingCodeLengthRule(255),
            LegacyEvChargeMappingCostRangeRule(7.9, 3976.8),
        ),
    )
}
