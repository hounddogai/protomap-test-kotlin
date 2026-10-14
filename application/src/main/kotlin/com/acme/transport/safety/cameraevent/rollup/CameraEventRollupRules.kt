package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize

/** A rule a camera event rollup must pass. Check returns its problems, or none. */
interface CameraEventRollupRule {
    fun check(item: CameraEventRollup): MutableList<String>
}

data class CameraEventRollupAllowedExternalRefRule(var allowed: MutableList<String>) : CameraEventRollupRule {
    override fun check(item: CameraEventRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRollupExternalRefLengthRule(var maxLength: Int) : CameraEventRollupRule {
    override fun check(item: CameraEventRollup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRollupCodePrefixRule(var prefix: String) : CameraEventRollupRule {
    override fun check(item: CameraEventRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRollupRevisionRangeRule(var min: Int, var max: Int) : CameraEventRollupRule {
    override fun check(item: CameraEventRollup): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class CameraEventRollupRequiredExternalRefRule(var message: String) : CameraEventRollupRule {
    override fun check(item: CameraEventRollup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event rollup. */
data class CameraEventRollupValidator(var rules: MutableList<CameraEventRollupRule>) {
    fun validate(item: CameraEventRollup): MutableList<String> {
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

    fun requireValid(item: CameraEventRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventRollupValidator(): CameraEventRollupValidator {
    return CameraEventRollupValidator(
        mutableListOf<CameraEventRollupRule>(
            CameraEventRollupAllowedExternalRefRule(mutableListOf<String>("scheduled")),
            CameraEventRollupExternalRefLengthRule(255),
            CameraEventRollupCodePrefixRule("REF"),
            CameraEventRollupRevisionRangeRule(32, 86400),
            CameraEventRollupRequiredExternalRefRule("camera event rollup needs this field"),
        ),
    )
}
