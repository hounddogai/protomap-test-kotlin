package com.acme.transport.network.cutoff.cutoffentry

/** A rule a default cutoff target must pass. Check returns its problems, or none. */
interface DefaultCutoffTargetRule {
    fun check(item: DefaultCutoffTarget): MutableList<String>
}

data class DefaultCutoffTargetCodeLengthRule(var maxLength: Int) : DefaultCutoffTargetRule {
    override fun check(item: DefaultCutoffTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffTargetUpdatedAtRangeRule(var min: Int, var max: Int) : DefaultCutoffTargetRule {
    override fun check(item: DefaultCutoffTarget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffTargetLimitRangeRule(var min: Int, var max: Int) : DefaultCutoffTargetRule {
    override fun check(item: DefaultCutoffTarget): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffTargetLabelLengthRule(var maxLength: Int) : DefaultCutoffTargetRule {
    override fun check(item: DefaultCutoffTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffTargetRequiredLabelRule(var message: String) : DefaultCutoffTargetRule {
    override fun check(item: DefaultCutoffTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff target. */
data class DefaultCutoffTargetValidator(var rules: MutableList<DefaultCutoffTargetRule>) {
    fun validate(item: DefaultCutoffTarget): MutableList<String> {
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

    fun requireValid(item: DefaultCutoffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffTargetValidator(): DefaultCutoffTargetValidator {
    return DefaultCutoffTargetValidator(
        mutableListOf<DefaultCutoffTargetRule>(
            DefaultCutoffTargetCodeLengthRule(1000),
            DefaultCutoffTargetUpdatedAtRangeRule(255, 3600),
            DefaultCutoffTargetLimitRangeRule(16, 80),
            DefaultCutoffTargetLabelLengthRule(120),
            DefaultCutoffTargetRequiredLabelRule("default cutoff target needs this field"),
        ),
    )
}
