package com.acme.transport.crewing.availability.manifest

/** A rule a lenient availability manifest must pass. Check returns its problems, or none. */
interface LenientAvailabilityManifestRule {
    fun check(item: LenientAvailabilityManifest): MutableList<String>
}

data class LenientAvailabilityManifestMaximumRangeRule(
    var min: Double,
    var max: Double,
) : LenientAvailabilityManifestRule {
    override fun check(item: LenientAvailabilityManifest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityManifestRouteCodeLengthRule(
    var maxLength: Int,
) : LenientAvailabilityManifestRule {
    override fun check(item: LenientAvailabilityManifest): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityManifestWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : LenientAvailabilityManifestRule {
    override fun check(item: LenientAvailabilityManifest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LenientAvailabilityManifestCodeLengthRule(var maxLength: Int) : LenientAvailabilityManifestRule {
    override fun check(item: LenientAvailabilityManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAvailabilityManifestRequiredCodeRule(var message: String) : LenientAvailabilityManifestRule {
    override fun check(item: LenientAvailabilityManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient availability manifest. */
data class LenientAvailabilityManifestValidator(var rules: MutableList<LenientAvailabilityManifestRule>) {
    fun validate(item: LenientAvailabilityManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAvailabilityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAvailabilityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAvailabilityManifestValidator(): LenientAvailabilityManifestValidator {
    return LenientAvailabilityManifestValidator(
        mutableListOf<LenientAvailabilityManifestRule>(
            LenientAvailabilityManifestMaximumRangeRule(2.8, 1609.4),
            LenientAvailabilityManifestRouteCodeLengthRule(32),
            LenientAvailabilityManifestWindowDaysRangeRule(120, 3600),
            LenientAvailabilityManifestCodeLengthRule(80),
            LenientAvailabilityManifestRequiredCodeRule("lenient availability manifest needs this field"),
        ),
    )
}
