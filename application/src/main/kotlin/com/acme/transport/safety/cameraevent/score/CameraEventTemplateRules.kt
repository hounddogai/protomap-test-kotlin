package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.text.normalize

/** A rule a camera event template must pass. Check returns its problems, or none. */
interface CameraEventTemplateRule {
    fun check(item: CameraEventTemplate): MutableList<String>
}

data class CameraEventTemplateAllowedCodeRule(var allowed: MutableList<String>) : CameraEventTemplateRule {
    override fun check(item: CameraEventTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTemplateRequiredCodeRule(var message: String) : CameraEventTemplateRule {
    override fun check(item: CameraEventTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTemplateCodePrefixRule(var prefix: String) : CameraEventTemplateRule {
    override fun check(item: CameraEventTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTemplateRequiredLabelRule(var message: String) : CameraEventTemplateRule {
    override fun check(item: CameraEventTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event template. */
data class CameraEventTemplateValidator(var rules: MutableList<CameraEventTemplateRule>) {
    fun validate(item: CameraEventTemplate): MutableList<String> {
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

    fun requireValid(item: CameraEventTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventTemplateValidator(): CameraEventTemplateValidator {
    return CameraEventTemplateValidator(
        mutableListOf<CameraEventTemplateRule>(
            CameraEventTemplateAllowedCodeRule(mutableListOf<String>("manual")),
            CameraEventTemplateRequiredCodeRule("camera event template needs this field"),
            CameraEventTemplateCodePrefixRule("OPS"),
            CameraEventTemplateRequiredLabelRule("camera event template needs this field"),
        ),
    )
}
