package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.normalize

/** A rule a camera event request must pass. Check returns its problems, or none. */
interface CameraEventRequestRule {
    fun check(item: CameraEventRequest): MutableList<String>
}

data class CameraEventRequestDockLengthRule(var maxLength: Int) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRequestRequiredCodeRule(var message: String) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRequestLabelLengthRule(var maxLength: Int) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRequestCostRangeRule(var min: Double, var max: Double) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRequestAllowedCodeRule(var allowed: MutableList<String>) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRequestCodeLengthRule(var maxLength: Int) : CameraEventRequestRule {
    override fun check(item: CameraEventRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event request. */
data class CameraEventRequestValidator(var rules: MutableList<CameraEventRequestRule>) {
    fun validate(item: CameraEventRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventRequestValidator(): CameraEventRequestValidator {
    return CameraEventRequestValidator(
        mutableListOf<CameraEventRequestRule>(
            CameraEventRequestDockLengthRule(64),
            CameraEventRequestRequiredCodeRule("camera event request needs this field"),
            CameraEventRequestLabelLengthRule(16),
            CameraEventRequestCostRangeRule(0.1, 970.3),
            CameraEventRequestAllowedCodeRule(mutableListOf<String>("regional", "batched", "batched")),
            CameraEventRequestCodeLengthRule(32),
        ),
    )
}
