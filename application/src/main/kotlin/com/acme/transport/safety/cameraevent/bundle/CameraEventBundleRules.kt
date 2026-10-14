package com.acme.transport.safety.cameraevent.bundle

/** A rule a camera event bundle must pass. Check returns its problems, or none. */
interface CameraEventBundleRule {
    fun check(item: CameraEventBundle): MutableList<String>
}

data class CameraEventBundleCodeLengthRule(var maxLength: Int) : CameraEventBundleRule {
    override fun check(item: CameraEventBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventBundleDiscountRangeRule(var min: Double, var max: Double) : CameraEventBundleRule {
    override fun check(item: CameraEventBundle): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class CameraEventBundleTitleLengthRule(var maxLength: Int) : CameraEventBundleRule {
    override fun check(item: CameraEventBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event bundle. */
data class CameraEventBundleValidator(var rules: MutableList<CameraEventBundleRule>) {
    fun validate(item: CameraEventBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventBundleValidator(): CameraEventBundleValidator {
    return CameraEventBundleValidator(
        mutableListOf<CameraEventBundleRule>(
            CameraEventBundleCodeLengthRule(64),
            CameraEventBundleDiscountRangeRule(7.7, 4179.6),
            CameraEventBundleTitleLengthRule(64),
        ),
    )
}
