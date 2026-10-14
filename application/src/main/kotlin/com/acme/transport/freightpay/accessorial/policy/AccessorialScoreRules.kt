package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize

/** A rule a accessorial score must pass. Check returns its problems, or none. */
interface AccessorialScoreRule {
    fun check(item: AccessorialScore): MutableList<String>
}

data class AccessorialScoreScoreRangeRule(var min: Double, var max: Double) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScoreAllowedSourceRule(var allowed: MutableList<String>) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScoreFloorValueRangeRule(var min: Double, var max: Double) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScoreRequiredSourceRule(var message: String) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScoreSourceLengthRule(var maxLength: Int) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScoreRequiredLabelRule(var message: String) : AccessorialScoreRule {
    override fun check(item: AccessorialScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial score. */
data class AccessorialScoreValidator(var rules: MutableList<AccessorialScoreRule>) {
    fun validate(item: AccessorialScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialScoreValidator(): AccessorialScoreValidator {
    return AccessorialScoreValidator(
        mutableListOf<AccessorialScoreRule>(
            AccessorialScoreScoreRangeRule(8.3, 293.5),
            AccessorialScoreAllowedSourceRule(mutableListOf<String>("adaptive")),
            AccessorialScoreFloorValueRangeRule(10.0, 1033.0),
            AccessorialScoreRequiredSourceRule("accessorial score needs a value"),
            AccessorialScoreSourceLengthRule(120),
            AccessorialScoreRequiredLabelRule("accessorial score needs an entry"),
        ),
    )
}
