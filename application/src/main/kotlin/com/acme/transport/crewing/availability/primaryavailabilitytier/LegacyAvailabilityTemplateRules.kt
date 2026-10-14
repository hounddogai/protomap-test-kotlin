package com.acme.transport.crewing.availability.primaryavailabilitytier

/** A rule a legacy availability template must pass. Check returns its problems, or none. */
interface LegacyAvailabilityTemplateRule {
    fun check(item: LegacyAvailabilityTemplate): MutableList<String>
}

data class LegacyAvailabilityTemplateCodePrefixRule(var prefix: String) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityTemplateRequiredDescriptionRule(
    var message: String,
) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityTemplateRequiredNameRule(var message: String) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityTemplateOwnerIdLengthRule(var maxLength: Int) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAvailabilityTemplateThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
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

data class LegacyAvailabilityTemplateCodeLengthRule(var maxLength: Int) : LegacyAvailabilityTemplateRule {
    override fun check(item: LegacyAvailabilityTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy availability template. */
data class LegacyAvailabilityTemplateValidator(var rules: MutableList<LegacyAvailabilityTemplateRule>) {
    fun validate(item: LegacyAvailabilityTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAvailabilityTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAvailabilityTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAvailabilityTemplateValidator(): LegacyAvailabilityTemplateValidator {
    return LegacyAvailabilityTemplateValidator(
        mutableListOf<LegacyAvailabilityTemplateRule>(
            LegacyAvailabilityTemplateCodePrefixRule("X"),
            LegacyAvailabilityTemplateRequiredDescriptionRule("legacy availability template needs an entry"),
            LegacyAvailabilityTemplateRequiredNameRule("legacy availability template needs a value"),
            LegacyAvailabilityTemplateOwnerIdLengthRule(16),
            LegacyAvailabilityTemplateThresholdRangeRule(1.7, 4644.2),
            LegacyAvailabilityTemplateCodeLengthRule(16),
        ),
    )
}
