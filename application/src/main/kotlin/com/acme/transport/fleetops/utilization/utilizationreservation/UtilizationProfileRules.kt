package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** A rule a utilization profile must pass. Check returns its problems, or none. */
interface UtilizationProfileRule {
    fun check(item: UtilizationProfile): MutableList<String>
}

data class UtilizationProfileAllowedUnitRule(var allowed: MutableList<String>) : UtilizationProfileRule {
    override fun check(item: UtilizationProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProfileRequiredChannelRule(var message: String) : UtilizationProfileRule {
    override fun check(item: UtilizationProfile): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProfileRequiredCodeRule(var message: String) : UtilizationProfileRule {
    override fun check(item: UtilizationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProfileCodeLengthRule(var maxLength: Int) : UtilizationProfileRule {
    override fun check(item: UtilizationProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization profile. */
data class UtilizationProfileValidator(var rules: MutableList<UtilizationProfileRule>) {
    fun validate(item: UtilizationProfile): MutableList<String> {
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

    fun requireValid(item: UtilizationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationProfileValidator(): UtilizationProfileValidator {
    return UtilizationProfileValidator(
        mutableListOf<UtilizationProfileRule>(
            UtilizationProfileAllowedUnitRule(mutableListOf<String>("tiered", "fallback", "manual")),
            UtilizationProfileRequiredChannelRule("utilization profile needs an entry"),
            UtilizationProfileRequiredCodeRule("utilization profile needs this field"),
            UtilizationProfileCodeLengthRule(120),
        ),
    )
}
