package com.acme.transport.emissions.evcharge.template

/** A rule a weighted ev charge claim must pass. Check returns its problems, or none. */
interface WeightedEvChargeClaimRule {
    fun check(item: WeightedEvChargeClaim): MutableList<String>
}

data class WeightedEvChargeClaimRequiredCodeRule(var message: String) : WeightedEvChargeClaimRule {
    override fun check(item: WeightedEvChargeClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeClaimTimezoneLengthRule(var maxLength: Int) : WeightedEvChargeClaimRule {
    override fun check(item: WeightedEvChargeClaim): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeClaimCodeLengthRule(var maxLength: Int) : WeightedEvChargeClaimRule {
    override fun check(item: WeightedEvChargeClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeClaimRequiredTimezoneRule(var message: String) : WeightedEvChargeClaimRule {
    override fun check(item: WeightedEvChargeClaim): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge claim. */
data class WeightedEvChargeClaimValidator(var rules: MutableList<WeightedEvChargeClaimRule>) {
    fun validate(item: WeightedEvChargeClaim): MutableList<String> {
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

    fun requireValid(item: WeightedEvChargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeClaimValidator(): WeightedEvChargeClaimValidator {
    return WeightedEvChargeClaimValidator(
        mutableListOf<WeightedEvChargeClaimRule>(
            WeightedEvChargeClaimRequiredCodeRule("weighted ev charge claim needs a value"),
            WeightedEvChargeClaimTimezoneLengthRule(16),
            WeightedEvChargeClaimCodeLengthRule(80),
            WeightedEvChargeClaimRequiredTimezoneRule("weighted ev charge claim needs this field"),
        ),
    )
}
