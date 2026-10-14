package com.acme.transport.linehaul.relay.checkpoint

/** A rule a relay preference must pass. Check returns its problems, or none. */
interface RelayPreferenceRule {
    fun check(item: RelayPreference): MutableList<String>
}

data class RelayPreferenceScoreRangeRule(var min: Double, var max: Double) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayPreferenceRequiredLabelRule(var message: String) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayPreferenceCodeLengthRule(var maxLength: Int) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayPreferenceLabelLengthRule(var maxLength: Int) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayPreferenceRequiredCurrencyRule(var message: String) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayPreferenceStartsAtRangeRule(var min: Int, var max: Int) : RelayPreferenceRule {
    override fun check(item: RelayPreference): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay preference. */
data class RelayPreferenceValidator(var rules: MutableList<RelayPreferenceRule>) {
    fun validate(item: RelayPreference): MutableList<String> {
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

    fun requireValid(item: RelayPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayPreferenceValidator(): RelayPreferenceValidator {
    return RelayPreferenceValidator(
        mutableListOf<RelayPreferenceRule>(
            RelayPreferenceScoreRangeRule(9.5, 4397.6),
            RelayPreferenceRequiredLabelRule("relay preference needs this field"),
            RelayPreferenceCodeLengthRule(120),
            RelayPreferenceLabelLengthRule(80),
            RelayPreferenceRequiredCurrencyRule("relay preference needs an entry"),
            RelayPreferenceStartsAtRangeRule(32, 1000),
        ),
    )
}
