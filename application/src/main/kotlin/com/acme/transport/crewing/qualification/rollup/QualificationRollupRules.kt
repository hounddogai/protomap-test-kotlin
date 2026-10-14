package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.normalize

/** A rule a qualification rollup must pass. Check returns its problems, or none. */
interface QualificationRollupRule {
    fun check(item: QualificationRollup): MutableList<String>
}

data class QualificationRollupLabelLengthRule(var maxLength: Int) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRollupCodeLengthRule(var maxLength: Int) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRollupRequiredLabelRule(var message: String) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationRollupCodePrefixRule(var prefix: String) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRollupRequiredCodeRule(var message: String) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationRollupAllowedCodeRule(var allowed: MutableList<String>) : QualificationRollupRule {
    override fun check(item: QualificationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification rollup. */
data class QualificationRollupValidator(var rules: MutableList<QualificationRollupRule>) {
    fun validate(item: QualificationRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationRollupValidator(): QualificationRollupValidator {
    return QualificationRollupValidator(
        mutableListOf<QualificationRollupRule>(
            QualificationRollupLabelLengthRule(80),
            QualificationRollupCodeLengthRule(120),
            QualificationRollupRequiredLabelRule("qualification rollup needs a value"),
            QualificationRollupCodePrefixRule("OPS"),
            QualificationRollupRequiredCodeRule("qualification rollup needs a value"),
            QualificationRollupAllowedCodeRule(mutableListOf<String>("legacy")),
        ),
    )
}
