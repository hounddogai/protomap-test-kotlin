package com.acme.transport.customs.declaration.metric

/** A rule a weighted declaration checkpoint must pass. Check returns its problems, or none. */
interface WeightedDeclarationCheckpointRule {
    fun check(item: WeightedDeclarationCheckpoint): MutableList<String>
}

data class WeightedDeclarationCheckpointCodePrefixRule(var prefix: String) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationCheckpointExternalRefLengthRule(
    var maxLength: Int,
) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationCheckpointLaneLengthRule(var maxLength: Int) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationCheckpointRevisionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class WeightedDeclarationCheckpointRequiredNameRule(
    var message: String,
) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationCheckpointCodeLengthRule(var maxLength: Int) : WeightedDeclarationCheckpointRule {
    override fun check(item: WeightedDeclarationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted declaration checkpoint. */
data class WeightedDeclarationCheckpointValidator(var rules: MutableList<WeightedDeclarationCheckpointRule>) {
    fun validate(item: WeightedDeclarationCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDeclarationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDeclarationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDeclarationCheckpointValidator(): WeightedDeclarationCheckpointValidator {
    return WeightedDeclarationCheckpointValidator(
        mutableListOf<WeightedDeclarationCheckpointRule>(
            WeightedDeclarationCheckpointCodePrefixRule("Q"),
            WeightedDeclarationCheckpointExternalRefLengthRule(32),
            WeightedDeclarationCheckpointLaneLengthRule(255),
            WeightedDeclarationCheckpointRevisionRangeRule(80, 1000),
            WeightedDeclarationCheckpointRequiredNameRule("weighted declaration checkpoint needs this field"),
            WeightedDeclarationCheckpointCodeLengthRule(255),
        ),
    )
}
