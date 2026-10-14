package com.acme.transport.customs.clearance.schedule

/** A rule a fallback clearance manifest must pass. Check returns its problems, or none. */
interface FallbackClearanceManifestRule {
    fun check(item: FallbackClearanceManifest): MutableList<String>
}

data class FallbackClearanceManifestLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackClearanceManifestRule {
    override fun check(item: FallbackClearanceManifest): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackClearanceManifestCodePrefixRule(var prefix: String) : FallbackClearanceManifestRule {
    override fun check(item: FallbackClearanceManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceManifestDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackClearanceManifestRule {
    override fun check(item: FallbackClearanceManifest): MutableList<String> {
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

data class FallbackClearanceManifestLabelLengthRule(var maxLength: Int) : FallbackClearanceManifestRule {
    override fun check(item: FallbackClearanceManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceManifestRequiredLabelRule(var message: String) : FallbackClearanceManifestRule {
    override fun check(item: FallbackClearanceManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback clearance manifest. */
data class FallbackClearanceManifestValidator(var rules: MutableList<FallbackClearanceManifestRule>) {
    fun validate(item: FallbackClearanceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackClearanceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackClearanceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackClearanceManifestValidator(): FallbackClearanceManifestValidator {
    return FallbackClearanceManifestValidator(
        mutableListOf<FallbackClearanceManifestRule>(
            FallbackClearanceManifestLoadKgRangeRule(9.7, 1038.5),
            FallbackClearanceManifestCodePrefixRule("OPS"),
            FallbackClearanceManifestDiscountRangeRule(2.5, 1077.3),
            FallbackClearanceManifestLabelLengthRule(32),
            FallbackClearanceManifestRequiredLabelRule("fallback clearance manifest needs a value"),
        ),
    )
}
