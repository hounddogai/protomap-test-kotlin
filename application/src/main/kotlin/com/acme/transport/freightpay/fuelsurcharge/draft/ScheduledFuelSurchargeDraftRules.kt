package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel surcharge draft must pass. Check returns its problems, or none. */
interface ScheduledFuelSurchargeDraftRule {
    fun check(item: ScheduledFuelSurchargeDraft): MutableList<String>
}

data class ScheduledFuelSurchargeDraftScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeDraftAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeDraftRequiredCodeRule(var message: String) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeDraftRequiredTitleRule(var message: String) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeDraftRequiredCarrierRule(
    var message: String,
) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeDraftCodeLengthRule(var maxLength: Int) : ScheduledFuelSurchargeDraftRule {
    override fun check(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel surcharge draft. */
data class ScheduledFuelSurchargeDraftValidator(var rules: MutableList<ScheduledFuelSurchargeDraftRule>) {
    fun validate(item: ScheduledFuelSurchargeDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledFuelSurchargeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelSurchargeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelSurchargeDraftValidator(): ScheduledFuelSurchargeDraftValidator {
    return ScheduledFuelSurchargeDraftValidator(
        mutableListOf<ScheduledFuelSurchargeDraftRule>(
            ScheduledFuelSurchargeDraftScoreRangeRule(4.4, 633.7),
            ScheduledFuelSurchargeDraftAllowedCodeRule(mutableListOf<String>("tiered", "default")),
            ScheduledFuelSurchargeDraftRequiredCodeRule("scheduled fuel surcharge draft needs an entry"),
            ScheduledFuelSurchargeDraftRequiredTitleRule("scheduled fuel surcharge draft needs this field"),
            ScheduledFuelSurchargeDraftRequiredCarrierRule("scheduled fuel surcharge draft needs this field"),
            ScheduledFuelSurchargeDraftCodeLengthRule(32),
        ),
    )
}
