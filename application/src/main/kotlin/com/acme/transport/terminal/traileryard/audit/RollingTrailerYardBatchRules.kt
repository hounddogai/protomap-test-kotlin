package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize

/** A rule a rolling trailer yard batch must pass. Check returns its problems, or none. */
interface RollingTrailerYardBatchRule {
    fun check(item: RollingTrailerYardBatch): MutableList<String>
}

data class RollingTrailerYardBatchAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingTrailerYardBatchRule {
    override fun check(item: RollingTrailerYardBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardBatchRequiredLabelRule(var message: String) : RollingTrailerYardBatchRule {
    override fun check(item: RollingTrailerYardBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardBatchRequiredCodeRule(var message: String) : RollingTrailerYardBatchRule {
    override fun check(item: RollingTrailerYardBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard batch. */
data class RollingTrailerYardBatchValidator(var rules: MutableList<RollingTrailerYardBatchRule>) {
    fun validate(item: RollingTrailerYardBatch): MutableList<String> {
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

    fun requireValid(item: RollingTrailerYardBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardBatchValidator(): RollingTrailerYardBatchValidator {
    return RollingTrailerYardBatchValidator(
        mutableListOf<RollingTrailerYardBatchRule>(
            RollingTrailerYardBatchAllowedLabelRule(mutableListOf<String>()),
            RollingTrailerYardBatchRequiredLabelRule("rolling trailer yard batch needs a value"),
            RollingTrailerYardBatchRequiredCodeRule("rolling trailer yard batch needs this field"),
        ),
    )
}
