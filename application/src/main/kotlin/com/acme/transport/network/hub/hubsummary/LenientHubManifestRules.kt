package com.acme.transport.network.hub.hubsummary

/** A rule a lenient hub manifest must pass. Check returns its problems, or none. */
interface LenientHubManifestRule {
    fun check(item: LenientHubManifest): MutableList<String>
}

data class LenientHubManifestRequiredTitleRule(var message: String) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHubManifestLocaleLengthRule(var maxLength: Int) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubManifestCodePrefixRule(var prefix: String) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubManifestRequiredCodeRule(var message: String) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHubManifestCodeLengthRule(var maxLength: Int) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubManifestFloorValueRangeRule(var min: Double, var max: Double) : LenientHubManifestRule {
    override fun check(item: LenientHubManifest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hub manifest. */
data class LenientHubManifestValidator(var rules: MutableList<LenientHubManifestRule>) {
    fun validate(item: LenientHubManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientHubManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHubManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHubManifestValidator(): LenientHubManifestValidator {
    return LenientHubManifestValidator(
        mutableListOf<LenientHubManifestRule>(
            LenientHubManifestRequiredTitleRule("lenient hub manifest needs a value"),
            LenientHubManifestLocaleLengthRule(86400),
            LenientHubManifestCodePrefixRule("ACM"),
            LenientHubManifestRequiredCodeRule("lenient hub manifest needs this field"),
            LenientHubManifestCodeLengthRule(3600),
            LenientHubManifestFloorValueRangeRule(9.4, 935.6),
        ),
    )
}
