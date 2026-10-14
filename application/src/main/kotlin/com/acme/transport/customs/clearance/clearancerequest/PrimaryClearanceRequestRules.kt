package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.text.normalize

/** A rule a primary clearance request must pass. Check returns its problems, or none. */
interface PrimaryClearanceRequestRule {
    fun check(item: PrimaryClearanceRequest): MutableList<String>
}

data class PrimaryClearanceRequestLabelLengthRule(var maxLength: Int) : PrimaryClearanceRequestRule {
    override fun check(item: PrimaryClearanceRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceRequestRequiredCodeRule(var message: String) : PrimaryClearanceRequestRule {
    override fun check(item: PrimaryClearanceRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceRequestAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryClearanceRequestRule {
    override fun check(item: PrimaryClearanceRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceRequestBatchSizeRangeRule(var min: Int, var max: Int) : PrimaryClearanceRequestRule {
    override fun check(item: PrimaryClearanceRequest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary clearance request. */
data class PrimaryClearanceRequestValidator(var rules: MutableList<PrimaryClearanceRequestRule>) {
    fun validate(item: PrimaryClearanceRequest): MutableList<String> {
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

    fun requireValid(item: PrimaryClearanceRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceRequestValidator(): PrimaryClearanceRequestValidator {
    return PrimaryClearanceRequestValidator(
        mutableListOf<PrimaryClearanceRequestRule>(
            PrimaryClearanceRequestLabelLengthRule(32),
            PrimaryClearanceRequestRequiredCodeRule("primary clearance request needs this field"),
            PrimaryClearanceRequestAllowedLabelRule(mutableListOf<String>("primary", "global")),
            PrimaryClearanceRequestBatchSizeRangeRule(1000, 86400),
        ),
    )
}
