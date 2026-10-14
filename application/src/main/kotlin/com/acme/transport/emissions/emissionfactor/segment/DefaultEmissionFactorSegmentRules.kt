package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.text.normalize

/** A rule a default emission factor segment must pass. Check returns its problems, or none. */
interface DefaultEmissionFactorSegmentRule {
    fun check(item: DefaultEmissionFactorSegment): MutableList<String>
}

data class DefaultEmissionFactorSegmentRequiredCodeRule(
    var message: String,
) : DefaultEmissionFactorSegmentRule {
    override fun check(item: DefaultEmissionFactorSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSegmentCodeLengthRule(var maxLength: Int) : DefaultEmissionFactorSegmentRule {
    override fun check(item: DefaultEmissionFactorSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSegmentLabelLengthRule(var maxLength: Int) : DefaultEmissionFactorSegmentRule {
    override fun check(item: DefaultEmissionFactorSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSegmentAllowedNameRule(
    var allowed: MutableList<String>,
) : DefaultEmissionFactorSegmentRule {
    override fun check(item: DefaultEmissionFactorSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorSegmentLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : DefaultEmissionFactorSegmentRule {
    override fun check(item: DefaultEmissionFactorSegment): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default emission factor segment. */
data class DefaultEmissionFactorSegmentValidator(var rules: MutableList<DefaultEmissionFactorSegmentRule>) {
    fun validate(item: DefaultEmissionFactorSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultEmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEmissionFactorSegmentValidator(): DefaultEmissionFactorSegmentValidator {
    return DefaultEmissionFactorSegmentValidator(
        mutableListOf<DefaultEmissionFactorSegmentRule>(
            DefaultEmissionFactorSegmentRequiredCodeRule("default emission factor segment needs a value"),
            DefaultEmissionFactorSegmentCodeLengthRule(32),
            DefaultEmissionFactorSegmentLabelLengthRule(32),
            DefaultEmissionFactorSegmentAllowedNameRule(mutableListOf<String>("default", "primary")),
            DefaultEmissionFactorSegmentLoadKgRangeRule(9.9, 59.0),
        ),
    )
}
