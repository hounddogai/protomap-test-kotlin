package com.acme.transport.safety.cameraevent.claim

/** A rule a camera event claim must pass. Check returns its problems, or none. */
interface CameraEventClaimRule {
    fun check(item: CameraEventClaim): MutableList<String>
}

data class CameraEventClaimCodePrefixRule(var prefix: String) : CameraEventClaimRule {
    override fun check(item: CameraEventClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventClaimCodeLengthRule(var maxLength: Int) : CameraEventClaimRule {
    override fun check(item: CameraEventClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventClaimSummaryLengthRule(var maxLength: Int) : CameraEventClaimRule {
    override fun check(item: CameraEventClaim): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventClaimDepotLengthRule(var maxLength: Int) : CameraEventClaimRule {
    override fun check(item: CameraEventClaim): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventClaimRequiredDepotRule(var message: String) : CameraEventClaimRule {
    override fun check(item: CameraEventClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event claim. */
data class CameraEventClaimValidator(var rules: MutableList<CameraEventClaimRule>) {
    fun validate(item: CameraEventClaim): MutableList<String> {
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

    fun requireValid(item: CameraEventClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventClaimValidator(): CameraEventClaimValidator {
    return CameraEventClaimValidator(
        mutableListOf<CameraEventClaimRule>(
            CameraEventClaimCodePrefixRule("X"),
            CameraEventClaimCodeLengthRule(16),
            CameraEventClaimSummaryLengthRule(120),
            CameraEventClaimDepotLengthRule(120),
            CameraEventClaimRequiredDepotRule("camera event claim needs this field"),
        ),
    )
}
