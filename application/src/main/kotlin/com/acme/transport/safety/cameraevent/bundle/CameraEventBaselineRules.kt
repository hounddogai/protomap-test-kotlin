package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.text.normalize

/** A rule a camera event baseline must pass. Check returns its problems, or none. */
interface CameraEventBaselineRule {
    fun check(item: CameraEventBaseline): MutableList<String>
}

data class CameraEventBaselineRequiredLabelRule(var message: String) : CameraEventBaselineRule {
    override fun check(item: CameraEventBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBaselinePhoneLengthRule(var maxLength: Int) : CameraEventBaselineRule {
    override fun check(item: CameraEventBaseline): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBaselineAllowedPhoneRule(var allowed: MutableList<String>) : CameraEventBaselineRule {
    override fun check(item: CameraEventBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBaselineRequiredCodeRule(var message: String) : CameraEventBaselineRule {
    override fun check(item: CameraEventBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event baseline. */
data class CameraEventBaselineValidator(var rules: MutableList<CameraEventBaselineRule>) {
    fun validate(item: CameraEventBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventBaselineValidator(): CameraEventBaselineValidator {
    return CameraEventBaselineValidator(
        mutableListOf<CameraEventBaselineRule>(
            CameraEventBaselineRequiredLabelRule("camera event baseline needs this field"),
            CameraEventBaselinePhoneLengthRule(3600),
            CameraEventBaselineAllowedPhoneRule(mutableListOf<String>("batched", "weighted", "legacy")),
            CameraEventBaselineRequiredCodeRule("camera event baseline needs an entry"),
        ),
    )
}
