package com.acme.transport.fleetops.tire.tirebudget

/** A rule a adaptive tire manifest must pass. Check returns its problems, or none. */
interface AdaptiveTireManifestRule {
    fun check(item: AdaptiveTireManifest): MutableList<String>
}

data class AdaptiveTireManifestCodePrefixRule(var prefix: String) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireManifestRequiredTitleRule(var message: String) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireManifestRequiredDriverIdRule(var message: String) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireManifestTitleLengthRule(var maxLength: Int) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireManifestRequiredDescriptionRule(var message: String) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTireManifestPriorityRangeRule(var min: Int, var max: Int) : AdaptiveTireManifestRule {
    override fun check(item: AdaptiveTireManifest): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tire manifest. */
data class AdaptiveTireManifestValidator(var rules: MutableList<AdaptiveTireManifestRule>) {
    fun validate(item: AdaptiveTireManifest): MutableList<String> {
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

    fun requireValid(item: AdaptiveTireManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTireManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTireManifestValidator(): AdaptiveTireManifestValidator {
    return AdaptiveTireManifestValidator(
        mutableListOf<AdaptiveTireManifestRule>(
            AdaptiveTireManifestCodePrefixRule("OPS"),
            AdaptiveTireManifestRequiredTitleRule("adaptive tire manifest needs an entry"),
            AdaptiveTireManifestRequiredDriverIdRule("adaptive tire manifest needs a value"),
            AdaptiveTireManifestTitleLengthRule(255),
            AdaptiveTireManifestRequiredDescriptionRule("adaptive tire manifest needs an entry"),
            AdaptiveTireManifestPriorityRangeRule(16, 32),
        ),
    )
}
