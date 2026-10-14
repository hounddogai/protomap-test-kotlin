package com.acme.transport.safety.cameraevent.allocation

/** A rule a lenient camera event policy must pass. Check returns its problems, or none. */
interface LenientCameraEventPolicyRule {
    fun check(item: LenientCameraEventPolicy): MutableList<String>
}

data class LenientCameraEventPolicyReviewerLengthRule(var maxLength: Int) : LenientCameraEventPolicyRule {
    override fun check(item: LenientCameraEventPolicy): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventPolicyCodePrefixRule(var prefix: String) : LenientCameraEventPolicyRule {
    override fun check(item: LenientCameraEventPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventPolicyRequiredCodeRule(var message: String) : LenientCameraEventPolicyRule {
    override fun check(item: LenientCameraEventPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient camera event policy. */
data class LenientCameraEventPolicyValidator(var rules: MutableList<LenientCameraEventPolicyRule>) {
    fun validate(item: LenientCameraEventPolicy): MutableList<String> {
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

    fun requireValid(item: LenientCameraEventPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCameraEventPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCameraEventPolicyValidator(): LenientCameraEventPolicyValidator {
    return LenientCameraEventPolicyValidator(
        mutableListOf<LenientCameraEventPolicyRule>(
            LenientCameraEventPolicyReviewerLengthRule(86400),
            LenientCameraEventPolicyCodePrefixRule("OPS"),
            LenientCameraEventPolicyRequiredCodeRule("lenient camera event policy needs an entry"),
        ),
    )
}
