package com.acme.transport.freightpay.accessorial.manifest

/** A rule a accessorial bundle must pass. Check returns its problems, or none. */
interface AccessorialBundleRule {
    fun check(item: AccessorialBundle): MutableList<String>
}

data class AccessorialBundleLabelLengthRule(var maxLength: Int) : AccessorialBundleRule {
    override fun check(item: AccessorialBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBundleLoadKgRangeRule(var min: Double, var max: Double) : AccessorialBundleRule {
    override fun check(item: AccessorialBundle): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBundleRequiredCodeRule(var message: String) : AccessorialBundleRule {
    override fun check(item: AccessorialBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBundleCodePrefixRule(var prefix: String) : AccessorialBundleRule {
    override fun check(item: AccessorialBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial bundle. */
data class AccessorialBundleValidator(var rules: MutableList<AccessorialBundleRule>) {
    fun validate(item: AccessorialBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialBundleValidator(): AccessorialBundleValidator {
    return AccessorialBundleValidator(
        mutableListOf<AccessorialBundleRule>(
            AccessorialBundleLabelLengthRule(120),
            AccessorialBundleLoadKgRangeRule(10.0, 4573.4),
            AccessorialBundleRequiredCodeRule("accessorial bundle needs an entry"),
            AccessorialBundleCodePrefixRule("Q"),
        ),
    )
}
