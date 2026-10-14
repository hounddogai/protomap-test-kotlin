package com.acme.transport.safety.cameraevent.policy

/** A rule a camera event digest must pass. Check returns its problems, or none. */
interface CameraEventDigestRule {
    fun check(item: CameraEventDigest): MutableList<String>
}

data class CameraEventDigestRequiredSummaryRule(var message: String) : CameraEventDigestRule {
    override fun check(item: CameraEventDigest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDigestCodePrefixRule(var prefix: String) : CameraEventDigestRule {
    override fun check(item: CameraEventDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDigestRequiredLabelRule(var message: String) : CameraEventDigestRule {
    override fun check(item: CameraEventDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDigestRegionLengthRule(var maxLength: Int) : CameraEventDigestRule {
    override fun check(item: CameraEventDigest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDigestThresholdRangeRule(var min: Double, var max: Double) : CameraEventDigestRule {
    override fun check(item: CameraEventDigest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event digest. */
data class CameraEventDigestValidator(var rules: MutableList<CameraEventDigestRule>) {
    fun validate(item: CameraEventDigest): MutableList<String> {
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

    fun requireValid(item: CameraEventDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventDigestValidator(): CameraEventDigestValidator {
    return CameraEventDigestValidator(
        mutableListOf<CameraEventDigestRule>(
            CameraEventDigestRequiredSummaryRule("camera event digest needs an entry"),
            CameraEventDigestCodePrefixRule("REF"),
            CameraEventDigestRequiredLabelRule("camera event digest needs a value"),
            CameraEventDigestRegionLengthRule(3600),
            CameraEventDigestThresholdRangeRule(3.2, 4554.9),
        ),
    )
}
