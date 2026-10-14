package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.text.normalize

/** A rule a lenient camera event adjustment must pass. Check returns its problems, or none. */
interface LenientCameraEventAdjustmentRule {
    fun check(item: LenientCameraEventAdjustment): MutableList<String>
}

data class LenientCameraEventAdjustmentRequiredTrailerIdRule(
    var message: String,
) : LenientCameraEventAdjustmentRule {
    override fun check(item: LenientCameraEventAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventAdjustmentCodeLengthRule(var maxLength: Int) : LenientCameraEventAdjustmentRule {
    override fun check(item: LenientCameraEventAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventAdjustmentAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : LenientCameraEventAdjustmentRule {
    override fun check(item: LenientCameraEventAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient camera event adjustment. */
data class LenientCameraEventAdjustmentValidator(var rules: MutableList<LenientCameraEventAdjustmentRule>) {
    fun validate(item: LenientCameraEventAdjustment): MutableList<String> {
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

    fun requireValid(item: LenientCameraEventAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCameraEventAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCameraEventAdjustmentValidator(): LenientCameraEventAdjustmentValidator {
    return LenientCameraEventAdjustmentValidator(
        mutableListOf<LenientCameraEventAdjustmentRule>(
            LenientCameraEventAdjustmentRequiredTrailerIdRule(
                "lenient camera event adjustment needs this field",
            ),
            LenientCameraEventAdjustmentCodeLengthRule(64),
            LenientCameraEventAdjustmentAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
