package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.normalize

/** A rule a regional parcel window must pass. Check returns its problems, or none. */
interface RegionalParcelWindowRule {
    fun check(item: RegionalParcelWindow): MutableList<String>
}

data class RegionalParcelWindowRetriesRangeRule(var min: Int, var max: Int) : RegionalParcelWindowRule {
    override fun check(item: RegionalParcelWindow): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class RegionalParcelWindowRequiredCodeRule(var message: String) : RegionalParcelWindowRule {
    override fun check(item: RegionalParcelWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelWindowAllowedSourceRule(var allowed: MutableList<String>) : RegionalParcelWindowRule {
    override fun check(item: RegionalParcelWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel window. */
data class RegionalParcelWindowValidator(var rules: MutableList<RegionalParcelWindowRule>) {
    fun validate(item: RegionalParcelWindow): MutableList<String> {
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

    fun requireValid(item: RegionalParcelWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelWindowValidator(): RegionalParcelWindowValidator {
    return RegionalParcelWindowValidator(
        mutableListOf<RegionalParcelWindowRule>(
            RegionalParcelWindowRetriesRangeRule(120, 255),
            RegionalParcelWindowRequiredCodeRule("regional parcel window needs a value"),
            RegionalParcelWindowAllowedSourceRule(mutableListOf<String>("rolling", "primary")),
        ),
    )
}
