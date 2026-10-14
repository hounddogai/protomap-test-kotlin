package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.text.normalize

/** A rule a capped incident baseline must pass. Check returns its problems, or none. */
interface CappedIncidentBaselineRule {
    fun check(item: CappedIncidentBaseline): MutableList<String>
}

data class CappedIncidentBaselineLabelLengthRule(var maxLength: Int) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentBaselineEtaMinutesRangeRule(var min: Int, var max: Int) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class CappedIncidentBaselineRequiredTimezoneRule(var message: String) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentBaselineAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentBaselineRequiredLabelRule(var message: String) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentBaselineRequiredCountryRule(var message: String) : CappedIncidentBaselineRule {
    override fun check(item: CappedIncidentBaseline): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped incident baseline. */
data class CappedIncidentBaselineValidator(var rules: MutableList<CappedIncidentBaselineRule>) {
    fun validate(item: CappedIncidentBaseline): MutableList<String> {
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

    fun requireValid(item: CappedIncidentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIncidentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIncidentBaselineValidator(): CappedIncidentBaselineValidator {
    return CappedIncidentBaselineValidator(
        mutableListOf<CappedIncidentBaselineRule>(
            CappedIncidentBaselineLabelLengthRule(16),
            CappedIncidentBaselineEtaMinutesRangeRule(32, 86400),
            CappedIncidentBaselineRequiredTimezoneRule("capped incident baseline needs a value"),
            CappedIncidentBaselineAllowedLabelRule(mutableListOf<String>("regional", "legacy", "strict")),
            CappedIncidentBaselineRequiredLabelRule("capped incident baseline needs this field"),
            CappedIncidentBaselineRequiredCountryRule("capped incident baseline needs this field"),
        ),
    )
}
