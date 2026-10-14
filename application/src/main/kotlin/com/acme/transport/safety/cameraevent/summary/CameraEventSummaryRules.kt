package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize

/** A rule a camera event summary must pass. Check returns its problems, or none. */
interface CameraEventSummaryRule {
    fun check(item: CameraEventSummary): MutableList<String>
}

data class CameraEventSummaryAllowedCodeRule(var allowed: MutableList<String>) : CameraEventSummaryRule {
    override fun check(item: CameraEventSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSummaryRequiredTitleRule(var message: String) : CameraEventSummaryRule {
    override fun check(item: CameraEventSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSummaryQuantityRangeRule(var min: Int, var max: Int) : CameraEventSummaryRule {
    override fun check(item: CameraEventSummary): MutableList<String> {
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

data class CameraEventSummaryRequiredRegionRule(var message: String) : CameraEventSummaryRule {
    override fun check(item: CameraEventSummary): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSummaryCodeLengthRule(var maxLength: Int) : CameraEventSummaryRule {
    override fun check(item: CameraEventSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event summary. */
data class CameraEventSummaryValidator(var rules: MutableList<CameraEventSummaryRule>) {
    fun validate(item: CameraEventSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventSummaryValidator(): CameraEventSummaryValidator {
    return CameraEventSummaryValidator(
        mutableListOf<CameraEventSummaryRule>(
            CameraEventSummaryAllowedCodeRule(mutableListOf<String>("legacy", "batched", "manual")),
            CameraEventSummaryRequiredTitleRule("camera event summary needs a value"),
            CameraEventSummaryQuantityRangeRule(32, 3600),
            CameraEventSummaryRequiredRegionRule("camera event summary needs an entry"),
            CameraEventSummaryCodeLengthRule(120),
        ),
    )
}
