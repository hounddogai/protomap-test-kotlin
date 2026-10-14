package com.acme.transport.linehaul.backhaul.threshold

/** A rule a fallback backhaul preference must pass. Check returns its problems, or none. */
interface FallbackBackhaulPreferenceRule {
    fun check(item: FallbackBackhaulPreference): MutableList<String>
}

data class FallbackBackhaulPreferenceRequiredContactEmailRule(
    var message: String,
) : FallbackBackhaulPreferenceRule {
    override fun check(item: FallbackBackhaulPreference): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulPreferenceRequiredTimezoneRule(
    var message: String,
) : FallbackBackhaulPreferenceRule {
    override fun check(item: FallbackBackhaulPreference): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulPreferenceRequiredCodeRule(var message: String) : FallbackBackhaulPreferenceRule {
    override fun check(item: FallbackBackhaulPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulPreferenceCodePrefixRule(var prefix: String) : FallbackBackhaulPreferenceRule {
    override fun check(item: FallbackBackhaulPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulPreferenceContactEmailLengthRule(
    var maxLength: Int,
) : FallbackBackhaulPreferenceRule {
    override fun check(item: FallbackBackhaulPreference): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul preference. */
data class FallbackBackhaulPreferenceValidator(var rules: MutableList<FallbackBackhaulPreferenceRule>) {
    fun validate(item: FallbackBackhaulPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulPreferenceValidator(): FallbackBackhaulPreferenceValidator {
    return FallbackBackhaulPreferenceValidator(
        mutableListOf<FallbackBackhaulPreferenceRule>(
            FallbackBackhaulPreferenceRequiredContactEmailRule("fallback backhaul preference needs this field"),
            FallbackBackhaulPreferenceRequiredTimezoneRule("fallback backhaul preference needs an entry"),
            FallbackBackhaulPreferenceRequiredCodeRule("fallback backhaul preference needs an entry"),
            FallbackBackhaulPreferenceCodePrefixRule("Q"),
            FallbackBackhaulPreferenceContactEmailLengthRule(1000),
        ),
    )
}
