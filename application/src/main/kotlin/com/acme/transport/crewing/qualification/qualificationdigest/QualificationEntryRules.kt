package com.acme.transport.crewing.qualification.qualificationdigest

/** A rule a qualification entry must pass. Check returns its problems, or none. */
interface QualificationEntryRule {
    fun check(item: QualificationEntry): MutableList<String>
}

data class QualificationEntryRevisionRangeRule(var min: Int, var max: Int) : QualificationEntryRule {
    override fun check(item: QualificationEntry): MutableList<String> {
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

data class QualificationEntryRequiredCategoryRule(var message: String) : QualificationEntryRule {
    override fun check(item: QualificationEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationEntryTrailerIdLengthRule(var maxLength: Int) : QualificationEntryRule {
    override fun check(item: QualificationEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationEntryCodeLengthRule(var maxLength: Int) : QualificationEntryRule {
    override fun check(item: QualificationEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationEntryCategoryLengthRule(var maxLength: Int) : QualificationEntryRule {
    override fun check(item: QualificationEntry): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification entry. */
data class QualificationEntryValidator(var rules: MutableList<QualificationEntryRule>) {
    fun validate(item: QualificationEntry): MutableList<String> {
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

    fun requireValid(item: QualificationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationEntryValidator(): QualificationEntryValidator {
    return QualificationEntryValidator(
        mutableListOf<QualificationEntryRule>(
            QualificationEntryRevisionRangeRule(16, 1000),
            QualificationEntryRequiredCategoryRule("qualification entry needs this field"),
            QualificationEntryTrailerIdLengthRule(64),
            QualificationEntryCodeLengthRule(64),
            QualificationEntryCategoryLengthRule(32),
        ),
    )
}
