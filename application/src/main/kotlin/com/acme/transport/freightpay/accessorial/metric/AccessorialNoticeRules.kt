package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.normalize

/** A rule a accessorial notice must pass. Check returns its problems, or none. */
interface AccessorialNoticeRule {
    fun check(item: AccessorialNotice): MutableList<String>
}

data class AccessorialNoticeRequiredCodeRule(var message: String) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialNoticeAllowedCodeRule(var allowed: MutableList<String>) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialNoticeBudgetRangeRule(var min: Double, var max: Double) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AccessorialNoticeRequiredLabelRule(var message: String) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialNoticeCeilingRangeRule(var min: Double, var max: Double) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AccessorialNoticeBatchSizeRangeRule(var min: Int, var max: Int) : AccessorialNoticeRule {
    override fun check(item: AccessorialNotice): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial notice. */
data class AccessorialNoticeValidator(var rules: MutableList<AccessorialNoticeRule>) {
    fun validate(item: AccessorialNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialNoticeValidator(): AccessorialNoticeValidator {
    return AccessorialNoticeValidator(
        mutableListOf<AccessorialNoticeRule>(
            AccessorialNoticeRequiredCodeRule("accessorial notice needs a value"),
            AccessorialNoticeAllowedCodeRule(mutableListOf<String>("lenient", "scheduled")),
            AccessorialNoticeBudgetRangeRule(0.2, 3514.9),
            AccessorialNoticeRequiredLabelRule("accessorial notice needs a value"),
            AccessorialNoticeCeilingRangeRule(6.2, 313.0),
            AccessorialNoticeBatchSizeRangeRule(16, 255),
        ),
    )
}
