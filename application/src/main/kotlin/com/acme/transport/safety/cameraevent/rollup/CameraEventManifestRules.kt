package com.acme.transport.safety.cameraevent.rollup

/** A rule a camera event manifest must pass. Check returns its problems, or none. */
interface CameraEventManifestRule {
    fun check(item: CameraEventManifest): MutableList<String>
}

data class CameraEventManifestCodePrefixRule(var prefix: String) : CameraEventManifestRule {
    override fun check(item: CameraEventManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventManifestQuantityRangeRule(var min: Int, var max: Int) : CameraEventManifestRule {
    override fun check(item: CameraEventManifest): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class CameraEventManifestRequiredNameRule(var message: String) : CameraEventManifestRule {
    override fun check(item: CameraEventManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event manifest. */
data class CameraEventManifestValidator(var rules: MutableList<CameraEventManifestRule>) {
    fun validate(item: CameraEventManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventManifestValidator(): CameraEventManifestValidator {
    return CameraEventManifestValidator(
        mutableListOf<CameraEventManifestRule>(
            CameraEventManifestCodePrefixRule("Q"),
            CameraEventManifestQuantityRangeRule(32, 86400),
            CameraEventManifestRequiredNameRule("camera event manifest needs an entry"),
        ),
    )
}
