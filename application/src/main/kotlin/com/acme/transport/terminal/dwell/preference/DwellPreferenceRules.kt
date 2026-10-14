package com.acme.transport.terminal.dwell.preference

/** A rule a dwell preference must pass. Check returns its problems, or none. */
interface DwellPreferenceRule {
    fun check(item: DwellPreference): MutableList<String>
}

data class DwellPreferenceLabelLengthRule(var maxLength: Int) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellPreferenceTrailerIdLengthRule(var maxLength: Int) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellPreferenceRequiredTrailerIdRule(var message: String) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellPreferenceTimezoneLengthRule(var maxLength: Int) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellPreferencePalletsRangeRule(var min: Int, var max: Int) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellPreferenceVisibleGuardRule(var limit: Double) : DwellPreferenceRule {
    override fun check(item: DwellPreference): MutableList<String> {
        if (item.visible && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell preference. */
data class DwellPreferenceValidator(var rules: MutableList<DwellPreferenceRule>) {
    fun validate(item: DwellPreference): MutableList<String> {
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

    fun requireValid(item: DwellPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellPreferenceValidator(): DwellPreferenceValidator {
    return DwellPreferenceValidator(
        mutableListOf<DwellPreferenceRule>(
            DwellPreferenceLabelLengthRule(32),
            DwellPreferenceTrailerIdLengthRule(64),
            DwellPreferenceRequiredTrailerIdRule("dwell preference needs a value"),
            DwellPreferenceTimezoneLengthRule(255),
            DwellPreferencePalletsRangeRule(120, 86400),
            DwellPreferenceVisibleGuardRule(5.3),
        ),
    )
}
