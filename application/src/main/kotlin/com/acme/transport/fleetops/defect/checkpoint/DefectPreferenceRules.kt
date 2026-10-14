package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a defect preference must pass. Check returns its problems, or none. */
interface DefectPreferenceRule {
    fun check(item: DefectPreference): MutableList<String>
}

data class DefectPreferenceBatchSizeRangeRule(var min: Int, var max: Int) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DefectPreferenceCodePrefixRule(var prefix: String) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectPreferenceRequiredDepotRule(var message: String) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectPreferenceAllowedDepotRule(var allowed: MutableList<String>) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefectPreferenceLimitRangeRule(var min: Int, var max: Int) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectPreferenceRequiredCodeRule(var message: String) : DefectPreferenceRule {
    override fun check(item: DefectPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect preference. */
data class DefectPreferenceValidator(var rules: MutableList<DefectPreferenceRule>) {
    fun validate(item: DefectPreference): MutableList<String> {
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

    fun requireValid(item: DefectPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectPreferenceValidator(): DefectPreferenceValidator {
    return DefectPreferenceValidator(
        mutableListOf<DefectPreferenceRule>(
            DefectPreferenceBatchSizeRangeRule(64, 80),
            DefectPreferenceCodePrefixRule("OPS"),
            DefectPreferenceRequiredDepotRule("defect preference needs this field"),
            DefectPreferenceAllowedDepotRule(mutableListOf<String>()),
            DefectPreferenceLimitRangeRule(32, 86400),
            DefectPreferenceRequiredCodeRule("defect preference needs a value"),
        ),
    )
}
