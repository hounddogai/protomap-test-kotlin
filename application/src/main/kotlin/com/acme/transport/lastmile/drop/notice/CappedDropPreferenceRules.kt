package com.acme.transport.lastmile.drop.notice

/** A rule a capped drop preference must pass. Check returns its problems, or none. */
interface CappedDropPreferenceRule {
    fun check(item: CappedDropPreference): MutableList<String>
}

data class CappedDropPreferenceRequiredPhoneRule(var message: String) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDropPreferencePalletsRangeRule(var min: Int, var max: Int) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class CappedDropPreferenceRequiredDepotRule(var message: String) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDropPreferenceRequiredCodeRule(var message: String) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDropPreferenceCodePrefixRule(var prefix: String) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDropPreferenceQuantityRangeRule(var min: Int, var max: Int) : CappedDropPreferenceRule {
    override fun check(item: CappedDropPreference): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped drop preference. */
data class CappedDropPreferenceValidator(var rules: MutableList<CappedDropPreferenceRule>) {
    fun validate(item: CappedDropPreference): MutableList<String> {
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

    fun requireValid(item: CappedDropPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDropPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDropPreferenceValidator(): CappedDropPreferenceValidator {
    return CappedDropPreferenceValidator(
        mutableListOf<CappedDropPreferenceRule>(
            CappedDropPreferenceRequiredPhoneRule("capped drop preference needs this field"),
            CappedDropPreferencePalletsRangeRule(120, 1000),
            CappedDropPreferenceRequiredDepotRule("capped drop preference needs this field"),
            CappedDropPreferenceRequiredCodeRule("capped drop preference needs an entry"),
            CappedDropPreferenceCodePrefixRule("X"),
            CappedDropPreferenceQuantityRangeRule(32, 86400),
        ),
    )
}
