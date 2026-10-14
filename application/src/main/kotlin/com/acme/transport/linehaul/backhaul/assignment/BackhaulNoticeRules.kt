package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.normalize

/** A rule a backhaul notice must pass. Check returns its problems, or none. */
interface BackhaulNoticeRule {
    fun check(item: BackhaulNotice): MutableList<String>
}

data class BackhaulNoticeDescriptionLengthRule(var maxLength: Int) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulNoticeQuantityRangeRule(var min: Int, var max: Int) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class BackhaulNoticeCodeLengthRule(var maxLength: Int) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulNoticeLabelLengthRule(var maxLength: Int) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulNoticeAllowedShiftCodeRule(var allowed: MutableList<String>) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulNoticeDistanceKmRangeRule(var min: Double, var max: Double) : BackhaulNoticeRule {
    override fun check(item: BackhaulNotice): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a backhaul notice. */
data class BackhaulNoticeValidator(var rules: MutableList<BackhaulNoticeRule>) {
    fun validate(item: BackhaulNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulNoticeValidator(): BackhaulNoticeValidator {
    return BackhaulNoticeValidator(
        mutableListOf<BackhaulNoticeRule>(
            BackhaulNoticeDescriptionLengthRule(64),
            BackhaulNoticeQuantityRangeRule(255, 1000),
            BackhaulNoticeCodeLengthRule(16),
            BackhaulNoticeLabelLengthRule(86400),
            BackhaulNoticeAllowedShiftCodeRule(mutableListOf<String>("pending")),
            BackhaulNoticeDistanceKmRangeRule(6.4, 2619.7),
        ),
    )
}
