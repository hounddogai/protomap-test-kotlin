package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.normalize

/** A rule a parcel policy must pass. Check returns its problems, or none. */
interface ParcelPolicyRule {
    fun check(item: ParcelPolicy): MutableList<String>
}

data class ParcelPolicyAllowedLabelRule(var allowed: MutableList<String>) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPolicyLabelLengthRule(var maxLength: Int) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPolicyRequiredDockRule(var message: String) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelPolicyTitleLengthRule(var maxLength: Int) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPolicyAttemptsRangeRule(var min: Int, var max: Int) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class ParcelPolicyRequiredLabelRule(var message: String) : ParcelPolicyRule {
    override fun check(item: ParcelPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel policy. */
data class ParcelPolicyValidator(var rules: MutableList<ParcelPolicyRule>) {
    fun validate(item: ParcelPolicy): MutableList<String> {
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

    fun requireValid(item: ParcelPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelPolicyValidator(): ParcelPolicyValidator {
    return ParcelPolicyValidator(
        mutableListOf<ParcelPolicyRule>(
            ParcelPolicyAllowedLabelRule(mutableListOf<String>("pending")),
            ParcelPolicyLabelLengthRule(86400),
            ParcelPolicyRequiredDockRule("parcel policy needs an entry"),
            ParcelPolicyTitleLengthRule(120),
            ParcelPolicyAttemptsRangeRule(32, 86400),
            ParcelPolicyRequiredLabelRule("parcel policy needs this field"),
        ),
    )
}
