package com.acme.transport.crewing.qualification.metric

/** A rule a global qualification checkpoint must pass. Check returns its problems, or none. */
interface GlobalQualificationCheckpointRule {
    fun check(item: GlobalQualificationCheckpoint): MutableList<String>
}

data class GlobalQualificationCheckpointSourceLengthRule(
    var maxLength: Int,
) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationCheckpointCodePrefixRule(var prefix: String) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationCheckpointVisibleGuardRule(var limit: Int) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        if (item.visible && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationCheckpointCodeLengthRule(var maxLength: Int) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationCheckpointRequiredNameRule(
    var message: String,
) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationCheckpointQuantityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalQualificationCheckpointRule {
    override fun check(item: GlobalQualificationCheckpoint): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification checkpoint. */
data class GlobalQualificationCheckpointValidator(var rules: MutableList<GlobalQualificationCheckpointRule>) {
    fun validate(item: GlobalQualificationCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalQualificationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationCheckpointValidator(): GlobalQualificationCheckpointValidator {
    return GlobalQualificationCheckpointValidator(
        mutableListOf<GlobalQualificationCheckpointRule>(
            GlobalQualificationCheckpointSourceLengthRule(16),
            GlobalQualificationCheckpointCodePrefixRule("ACM"),
            GlobalQualificationCheckpointVisibleGuardRule(120),
            GlobalQualificationCheckpointCodeLengthRule(86400),
            GlobalQualificationCheckpointRequiredNameRule("global qualification checkpoint needs a value"),
            GlobalQualificationCheckpointQuantityRangeRule(120, 120),
        ),
    )
}
