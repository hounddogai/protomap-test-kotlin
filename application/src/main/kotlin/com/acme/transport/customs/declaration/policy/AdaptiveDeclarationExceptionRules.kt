package com.acme.transport.customs.declaration.policy

/** A rule a adaptive declaration exception must pass. Check returns its problems, or none. */
interface AdaptiveDeclarationExceptionRule {
    fun check(item: AdaptiveDeclarationException): MutableList<String>
}

data class AdaptiveDeclarationExceptionCodePrefixRule(var prefix: String) : AdaptiveDeclarationExceptionRule {
    override fun check(item: AdaptiveDeclarationException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationExceptionStopCountRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveDeclarationExceptionRule {
    override fun check(item: AdaptiveDeclarationException): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDeclarationExceptionOwnerIdLengthRule(
    var maxLength: Int,
) : AdaptiveDeclarationExceptionRule {
    override fun check(item: AdaptiveDeclarationException): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationExceptionRequiredCodeRule(
    var message: String,
) : AdaptiveDeclarationExceptionRule {
    override fun check(item: AdaptiveDeclarationException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDeclarationExceptionRequiredLabelRule(
    var message: String,
) : AdaptiveDeclarationExceptionRule {
    override fun check(item: AdaptiveDeclarationException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive declaration exception. */
data class AdaptiveDeclarationExceptionValidator(var rules: MutableList<AdaptiveDeclarationExceptionRule>) {
    fun validate(item: AdaptiveDeclarationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDeclarationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDeclarationException>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDeclarationExceptionValidator(): AdaptiveDeclarationExceptionValidator {
    return AdaptiveDeclarationExceptionValidator(
        mutableListOf<AdaptiveDeclarationExceptionRule>(
            AdaptiveDeclarationExceptionCodePrefixRule("OPS"),
            AdaptiveDeclarationExceptionStopCountRangeRule(64, 1000),
            AdaptiveDeclarationExceptionOwnerIdLengthRule(1000),
            AdaptiveDeclarationExceptionRequiredCodeRule("adaptive declaration exception needs an entry"),
            AdaptiveDeclarationExceptionRequiredLabelRule("adaptive declaration exception needs an entry"),
        ),
    )
}
