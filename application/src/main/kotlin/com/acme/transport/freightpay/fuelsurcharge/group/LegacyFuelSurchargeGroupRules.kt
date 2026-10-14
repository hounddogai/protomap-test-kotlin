package com.acme.transport.freightpay.fuelsurcharge.group

/** A rule a legacy fuel surcharge group must pass. Check returns its problems, or none. */
interface LegacyFuelSurchargeGroupRule {
    fun check(item: LegacyFuelSurchargeGroup): MutableList<String>
}

data class LegacyFuelSurchargeGroupRequiredCodeRule(var message: String) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeGroupWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeGroupNotesLengthRule(var maxLength: Int) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeGroupRequiredTimezoneRule(var message: String) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeGroupCodePrefixRule(var prefix: String) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeGroupCodeLengthRule(var maxLength: Int) : LegacyFuelSurchargeGroupRule {
    override fun check(item: LegacyFuelSurchargeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy fuel surcharge group. */
data class LegacyFuelSurchargeGroupValidator(var rules: MutableList<LegacyFuelSurchargeGroupRule>) {
    fun validate(item: LegacyFuelSurchargeGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyFuelSurchargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFuelSurchargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFuelSurchargeGroupValidator(): LegacyFuelSurchargeGroupValidator {
    return LegacyFuelSurchargeGroupValidator(
        mutableListOf<LegacyFuelSurchargeGroupRule>(
            LegacyFuelSurchargeGroupRequiredCodeRule("legacy fuel surcharge group needs a value"),
            LegacyFuelSurchargeGroupWindowDaysRangeRule(32, 120),
            LegacyFuelSurchargeGroupNotesLengthRule(16),
            LegacyFuelSurchargeGroupRequiredTimezoneRule("legacy fuel surcharge group needs an entry"),
            LegacyFuelSurchargeGroupCodePrefixRule("REF"),
            LegacyFuelSurchargeGroupCodeLengthRule(1000),
        ),
    )
}
