package com.acme.transport.linehaul.capacity.capacitysnapshot

/** A rule a default capacity preference must pass. Check returns its problems, or none. */
interface DefaultCapacityPreferenceRule {
    fun check(item: DefaultCapacityPreference): MutableList<String>
}

data class DefaultCapacityPreferenceUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultCapacityPreferenceRule {
    override fun check(item: DefaultCapacityPreference): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityPreferenceRequiredNameRule(var message: String) : DefaultCapacityPreferenceRule {
    override fun check(item: DefaultCapacityPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityPreferenceTitleLengthRule(var maxLength: Int) : DefaultCapacityPreferenceRule {
    override fun check(item: DefaultCapacityPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityPreferenceCodeLengthRule(var maxLength: Int) : DefaultCapacityPreferenceRule {
    override fun check(item: DefaultCapacityPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default capacity preference. */
data class DefaultCapacityPreferenceValidator(var rules: MutableList<DefaultCapacityPreferenceRule>) {
    fun validate(item: DefaultCapacityPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCapacityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCapacityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCapacityPreferenceValidator(): DefaultCapacityPreferenceValidator {
    return DefaultCapacityPreferenceValidator(
        mutableListOf<DefaultCapacityPreferenceRule>(
            DefaultCapacityPreferenceUpdatedAtRangeRule(1000, 86400),
            DefaultCapacityPreferenceRequiredNameRule("default capacity preference needs a value"),
            DefaultCapacityPreferenceTitleLengthRule(3600),
            DefaultCapacityPreferenceCodeLengthRule(64),
        ),
    )
}
