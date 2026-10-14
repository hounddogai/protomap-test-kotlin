package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize

/** A rule a hub score must pass. Check returns its problems, or none. */
interface HubScoreRule {
    fun check(item: HubScore): MutableList<String>
}

data class HubScoreCodePrefixRule(var prefix: String) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubScoreAllowedOwnerIdRule(var allowed: MutableList<String>) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class HubScoreRequiredOwnerIdRule(var message: String) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubScoreThresholdRangeRule(var min: Double, var max: Double) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubScoreRequiredSummaryRule(var message: String) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubScoreLabelLengthRule(var maxLength: Int) : HubScoreRule {
    override fun check(item: HubScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub score. */
data class HubScoreValidator(var rules: MutableList<HubScoreRule>) {
    fun validate(item: HubScore): MutableList<String> {
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

    fun requireValid(item: HubScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubScore>): MutableMap<String, MutableList<String>> {
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

fun defaultHubScoreValidator(): HubScoreValidator {
    return HubScoreValidator(
        mutableListOf<HubScoreRule>(
            HubScoreCodePrefixRule("OPS"),
            HubScoreAllowedOwnerIdRule(mutableListOf<String>("manual", "capped", "tiered")),
            HubScoreRequiredOwnerIdRule("hub score needs this field"),
            HubScoreThresholdRangeRule(7.5, 1220.7),
            HubScoreRequiredSummaryRule("hub score needs a value"),
            HubScoreLabelLengthRule(32),
        ),
    )
}
