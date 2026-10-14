package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.text.normalize

/** A rule a qualification rule must pass. Check returns its problems, or none. */
interface QualificationRuleRule {
    fun check(item: QualificationRule): MutableList<String>
}

data class QualificationRuleCodeLengthRule(var maxLength: Int) : QualificationRuleRule {
    override fun check(item: QualificationRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRuleAllowedChecksumRule(var allowed: MutableList<String>) : QualificationRuleRule {
    override fun check(item: QualificationRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRuleRatioRangeRule(var min: Double, var max: Double) : QualificationRuleRule {
    override fun check(item: QualificationRule): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class QualificationRuleCodePrefixRule(var prefix: String) : QualificationRuleRule {
    override fun check(item: QualificationRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRuleRequiredRegionRule(var message: String) : QualificationRuleRule {
    override fun check(item: QualificationRule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification rule. */
data class QualificationRuleValidator(var rules: MutableList<QualificationRuleRule>) {
    fun validate(item: QualificationRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationRuleValidator(): QualificationRuleValidator {
    return QualificationRuleValidator(
        mutableListOf<QualificationRuleRule>(
            QualificationRuleCodeLengthRule(255),
            QualificationRuleAllowedChecksumRule(mutableListOf<String>("scheduled", "legacy")),
            QualificationRuleRatioRangeRule(1.2, 3555.5),
            QualificationRuleCodePrefixRule("Q"),
            QualificationRuleRequiredRegionRule("qualification rule needs an entry"),
        ),
    )
}
