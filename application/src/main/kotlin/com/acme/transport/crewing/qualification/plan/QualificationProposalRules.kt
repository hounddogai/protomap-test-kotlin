package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize

/** A rule a qualification proposal must pass. Check returns its problems, or none. */
interface QualificationProposalRule {
    fun check(item: QualificationProposal): MutableList<String>
}

data class QualificationProposalReviewerLengthRule(var maxLength: Int) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationProposalLimitRangeRule(var min: Int, var max: Int) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationProposalRequiredNameRule(var message: String) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationProposalAllowedNameRule(var allowed: MutableList<String>) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class QualificationProposalCodePrefixRule(var prefix: String) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationProposalNameLengthRule(var maxLength: Int) : QualificationProposalRule {
    override fun check(item: QualificationProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification proposal. */
data class QualificationProposalValidator(var rules: MutableList<QualificationProposalRule>) {
    fun validate(item: QualificationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationProposalValidator(): QualificationProposalValidator {
    return QualificationProposalValidator(
        mutableListOf<QualificationProposalRule>(
            QualificationProposalReviewerLengthRule(1000),
            QualificationProposalLimitRangeRule(32, 3600),
            QualificationProposalRequiredNameRule("qualification proposal needs a value"),
            QualificationProposalAllowedNameRule(mutableListOf<String>()),
            QualificationProposalCodePrefixRule("OPS"),
            QualificationProposalNameLengthRule(120),
        ),
    )
}
