package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize

/** A rule a camera event record must pass. Check returns its problems, or none. */
interface CameraEventRecordRule {
    fun check(item: CameraEventRecord): MutableList<String>
}

data class CameraEventRecordRequiredNameRule(var message: String) : CameraEventRecordRule {
    override fun check(item: CameraEventRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRecordCodePrefixRule(var prefix: String) : CameraEventRecordRule {
    override fun check(item: CameraEventRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRecordCarrierLengthRule(var maxLength: Int) : CameraEventRecordRule {
    override fun check(item: CameraEventRecord): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventRecordAllowedNameRule(var allowed: MutableList<String>) : CameraEventRecordRule {
    override fun check(item: CameraEventRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event record. */
data class CameraEventRecordValidator(var rules: MutableList<CameraEventRecordRule>) {
    fun validate(item: CameraEventRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventRecordValidator(): CameraEventRecordValidator {
    return CameraEventRecordValidator(
        mutableListOf<CameraEventRecordRule>(
            CameraEventRecordRequiredNameRule("camera event record needs this field"),
            CameraEventRecordCodePrefixRule("ACM"),
            CameraEventRecordCarrierLengthRule(255),
            CameraEventRecordAllowedNameRule(mutableListOf<String>("adaptive", "lenient")),
        ),
    )
}
