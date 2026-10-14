package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize

/** A rule a fallback capacity preference must pass. Check returns its problems, or none. */
interface FallbackCapacityPreferenceRule {
    fun check(item: FallbackCapacityPreference): MutableList<String>
}

data class FallbackCapacityPreferenceRequiredDepotRule(var message: String) : FallbackCapacityPreferenceRule {
    override fun check(item: FallbackCapacityPreference): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityPreferenceRequiredCodeRule(var message: String) : FallbackCapacityPreferenceRule {
    override fun check(item: FallbackCapacityPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityPreferenceAllowedDepotRule(
    var allowed: MutableList<String>,
) : FallbackCapacityPreferenceRule {
    override fun check(item: FallbackCapacityPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityPreferenceCodeLengthRule(var maxLength: Int) : FallbackCapacityPreferenceRule {
    override fun check(item: FallbackCapacityPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback capacity preference. */
data class FallbackCapacityPreferenceValidator(var rules: MutableList<FallbackCapacityPreferenceRule>) {
    fun validate(item: FallbackCapacityPreference): MutableList<String> {
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

    fun requireValid(item: FallbackCapacityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCapacityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCapacityPreferenceValidator(): FallbackCapacityPreferenceValidator {
    return FallbackCapacityPreferenceValidator(
        mutableListOf<FallbackCapacityPreferenceRule>(
            FallbackCapacityPreferenceRequiredDepotRule("fallback capacity preference needs a value"),
            FallbackCapacityPreferenceRequiredCodeRule("fallback capacity preference needs an entry"),
            FallbackCapacityPreferenceAllowedDepotRule(mutableListOf<String>("legacy", "pending", "default")),
            FallbackCapacityPreferenceCodeLengthRule(32),
        ),
    )
}
