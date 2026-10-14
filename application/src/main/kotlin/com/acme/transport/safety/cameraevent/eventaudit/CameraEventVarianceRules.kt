package com.acme.transport.safety.cameraevent.eventaudit

/** A rule a camera event variance must pass. Check returns its problems, or none. */
interface CameraEventVarianceRule {
    fun check(item: CameraEventVariance): MutableList<String>
}

data class CameraEventVarianceUnitLengthRule(var maxLength: Int) : CameraEventVarianceRule {
    override fun check(item: CameraEventVariance): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventVarianceCodePrefixRule(var prefix: String) : CameraEventVarianceRule {
    override fun check(item: CameraEventVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventVarianceReviewerLengthRule(var maxLength: Int) : CameraEventVarianceRule {
    override fun check(item: CameraEventVariance): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventVarianceRequiredNameRule(var message: String) : CameraEventVarianceRule {
    override fun check(item: CameraEventVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event variance. */
data class CameraEventVarianceValidator(var rules: MutableList<CameraEventVarianceRule>) {
    fun validate(item: CameraEventVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventVarianceValidator(): CameraEventVarianceValidator {
    return CameraEventVarianceValidator(
        mutableListOf<CameraEventVarianceRule>(
            CameraEventVarianceUnitLengthRule(120),
            CameraEventVarianceCodePrefixRule("ACM"),
            CameraEventVarianceReviewerLengthRule(120),
            CameraEventVarianceRequiredNameRule("camera event variance needs this field"),
        ),
    )
}
