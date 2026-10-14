package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.text.normalize

/** A rule a global hold batch must pass. Check returns its problems, or none. */
interface GlobalHoldBatchRule {
    fun check(item: GlobalHoldBatch): MutableList<String>
}

data class GlobalHoldBatchCodeLengthRule(var maxLength: Int) : GlobalHoldBatchRule {
    override fun check(item: GlobalHoldBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldBatchRequiredSegmentRule(var message: String) : GlobalHoldBatchRule {
    override fun check(item: GlobalHoldBatch): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldBatchSegmentLengthRule(var maxLength: Int) : GlobalHoldBatchRule {
    override fun check(item: GlobalHoldBatch): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldBatchAllowedLabelRule(var allowed: MutableList<String>) : GlobalHoldBatchRule {
    override fun check(item: GlobalHoldBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldBatchRetriesRangeRule(var min: Int, var max: Int) : GlobalHoldBatchRule {
    override fun check(item: GlobalHoldBatch): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global hold batch. */
data class GlobalHoldBatchValidator(var rules: MutableList<GlobalHoldBatchRule>) {
    fun validate(item: GlobalHoldBatch): MutableList<String> {
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

    fun requireValid(item: GlobalHoldBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHoldBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHoldBatchValidator(): GlobalHoldBatchValidator {
    return GlobalHoldBatchValidator(
        mutableListOf<GlobalHoldBatchRule>(
            GlobalHoldBatchCodeLengthRule(32),
            GlobalHoldBatchRequiredSegmentRule("global hold batch needs this field"),
            GlobalHoldBatchSegmentLengthRule(64),
            GlobalHoldBatchAllowedLabelRule(mutableListOf<String>("pending", "manual")),
            GlobalHoldBatchRetriesRangeRule(1000, 3600),
        ),
    )
}
