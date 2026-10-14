package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize

/** A rule a rolling accessorial batch must pass. Check returns its problems, or none. */
interface RollingAccessorialBatchRule {
    fun check(item: RollingAccessorialBatch): MutableList<String>
}

data class RollingAccessorialBatchRequiredNameRule(var message: String) : RollingAccessorialBatchRule {
    override fun check(item: RollingAccessorialBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialBatchScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingAccessorialBatchRule {
    override fun check(item: RollingAccessorialBatch): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialBatchDriverIdLengthRule(var maxLength: Int) : RollingAccessorialBatchRule {
    override fun check(item: RollingAccessorialBatch): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialBatchRequiredSummaryRule(var message: String) : RollingAccessorialBatchRule {
    override fun check(item: RollingAccessorialBatch): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialBatchAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingAccessorialBatchRule {
    override fun check(item: RollingAccessorialBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling accessorial batch. */
data class RollingAccessorialBatchValidator(var rules: MutableList<RollingAccessorialBatchRule>) {
    fun validate(item: RollingAccessorialBatch): MutableList<String> {
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

    fun requireValid(item: RollingAccessorialBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAccessorialBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAccessorialBatchValidator(): RollingAccessorialBatchValidator {
    return RollingAccessorialBatchValidator(
        mutableListOf<RollingAccessorialBatchRule>(
            RollingAccessorialBatchRequiredNameRule("rolling accessorial batch needs this field"),
            RollingAccessorialBatchScoreRangeRule(5.7, 2909.1),
            RollingAccessorialBatchDriverIdLengthRule(120),
            RollingAccessorialBatchRequiredSummaryRule("rolling accessorial batch needs an entry"),
            RollingAccessorialBatchAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
