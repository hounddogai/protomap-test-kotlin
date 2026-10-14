package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize

/** A rule a default defect agreement must pass. Check returns its problems, or none. */
interface DefaultDefectAgreementRule {
    fun check(item: DefaultDefectAgreement): MutableList<String>
}

data class DefaultDefectAgreementNameLengthRule(var maxLength: Int) : DefaultDefectAgreementRule {
    override fun check(item: DefaultDefectAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectAgreementDriverIdLengthRule(var maxLength: Int) : DefaultDefectAgreementRule {
    override fun check(item: DefaultDefectAgreement): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectAgreementMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultDefectAgreementRule {
    override fun check(item: DefaultDefectAgreement): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectAgreementAllowedNameRule(
    var allowed: MutableList<String>,
) : DefaultDefectAgreementRule {
    override fun check(item: DefaultDefectAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectAgreementRequiredCodeRule(var message: String) : DefaultDefectAgreementRule {
    override fun check(item: DefaultDefectAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default defect agreement. */
data class DefaultDefectAgreementValidator(var rules: MutableList<DefaultDefectAgreementRule>) {
    fun validate(item: DefaultDefectAgreement): MutableList<String> {
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

    fun requireValid(item: DefaultDefectAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDefectAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDefectAgreementValidator(): DefaultDefectAgreementValidator {
    return DefaultDefectAgreementValidator(
        mutableListOf<DefaultDefectAgreementRule>(
            DefaultDefectAgreementNameLengthRule(3600),
            DefaultDefectAgreementDriverIdLengthRule(1000),
            DefaultDefectAgreementMinimumRangeRule(8.3, 670.4),
            DefaultDefectAgreementAllowedNameRule(mutableListOf<String>()),
            DefaultDefectAgreementRequiredCodeRule("default defect agreement needs an entry"),
        ),
    )
}
