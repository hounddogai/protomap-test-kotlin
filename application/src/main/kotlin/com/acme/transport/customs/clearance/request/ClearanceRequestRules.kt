package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.text.normalize

/** A rule a clearance request must pass. Check returns its problems, or none. */
interface ClearanceRequestRule {
    fun check(item: ClearanceRequest): MutableList<String>
}

data class ClearanceRequestCodeLengthRule(var maxLength: Int) : ClearanceRequestRule {
    override fun check(item: ClearanceRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRequestAllowedCodeRule(var allowed: MutableList<String>) : ClearanceRequestRule {
    override fun check(item: ClearanceRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRequestBatchSizeRangeRule(var min: Int, var max: Int) : ClearanceRequestRule {
    override fun check(item: ClearanceRequest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRequestThresholdRangeRule(var min: Double, var max: Double) : ClearanceRequestRule {
    override fun check(item: ClearanceRequest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance request. */
data class ClearanceRequestValidator(var rules: MutableList<ClearanceRequestRule>) {
    fun validate(item: ClearanceRequest): MutableList<String> {
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

    fun requireValid(item: ClearanceRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceRequestValidator(): ClearanceRequestValidator {
    return ClearanceRequestValidator(
        mutableListOf<ClearanceRequestRule>(
            ClearanceRequestCodeLengthRule(32),
            ClearanceRequestAllowedCodeRule(mutableListOf<String>("batched", "regional", "manual")),
            ClearanceRequestBatchSizeRangeRule(64, 80),
            ClearanceRequestThresholdRangeRule(9.6, 3161.1),
        ),
    )
}
