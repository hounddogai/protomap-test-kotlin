package com.acme.transport.crewing.qualification.milestone

/** A rule a qualification mapping must pass. Check returns its problems, or none. */
interface QualificationMappingRule {
    fun check(item: QualificationMapping): MutableList<String>
}

data class QualificationMappingNameLengthRule(var maxLength: Int) : QualificationMappingRule {
    override fun check(item: QualificationMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMappingCodeLengthRule(var maxLength: Int) : QualificationMappingRule {
    override fun check(item: QualificationMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMappingCodePrefixRule(var prefix: String) : QualificationMappingRule {
    override fun check(item: QualificationMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification mapping. */
data class QualificationMappingValidator(var rules: MutableList<QualificationMappingRule>) {
    fun validate(item: QualificationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationMappingValidator(): QualificationMappingValidator {
    return QualificationMappingValidator(
        mutableListOf<QualificationMappingRule>(
            QualificationMappingNameLengthRule(255),
            QualificationMappingCodeLengthRule(32),
            QualificationMappingCodePrefixRule("REF"),
        ),
    )
}
