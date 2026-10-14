package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.normalize

/** A rule a backhaul threshold must pass. Check returns its problems, or none. */
interface BackhaulThresholdRule {
    fun check(item: BackhaulThreshold): MutableList<String>
}

data class BackhaulThresholdRequiredLabelRule(var message: String) : BackhaulThresholdRule {
    override fun check(item: BackhaulThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulThresholdGraceDaysRangeRule(var min: Int, var max: Int) : BackhaulThresholdRule {
    override fun check(item: BackhaulThreshold): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulThresholdNameLengthRule(var maxLength: Int) : BackhaulThresholdRule {
    override fun check(item: BackhaulThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulThresholdAllowedLabelRule(var allowed: MutableList<String>) : BackhaulThresholdRule {
    override fun check(item: BackhaulThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul threshold. */
data class BackhaulThresholdValidator(var rules: MutableList<BackhaulThresholdRule>) {
    fun validate(item: BackhaulThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulThresholdValidator(): BackhaulThresholdValidator {
    return BackhaulThresholdValidator(
        mutableListOf<BackhaulThresholdRule>(
            BackhaulThresholdRequiredLabelRule("backhaul threshold needs an entry"),
            BackhaulThresholdGraceDaysRangeRule(80, 86400),
            BackhaulThresholdNameLengthRule(1000),
            BackhaulThresholdAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
