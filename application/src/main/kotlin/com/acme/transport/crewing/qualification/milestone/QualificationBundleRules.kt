package com.acme.transport.crewing.qualification.milestone

/** A rule a qualification bundle must pass. Check returns its problems, or none. */
interface QualificationBundleRule {
    fun check(item: QualificationBundle): MutableList<String>
}

data class QualificationBundleRequiredContactEmailRule(var message: String) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationBundleRequiredCodeRule(var message: String) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationBundleNameLengthRule(var maxLength: Int) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationBundleContactEmailLengthRule(var maxLength: Int) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationBundleSegmentLengthRule(var maxLength: Int) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationBundleMarginRangeRule(var min: Double, var max: Double) : QualificationBundleRule {
    override fun check(item: QualificationBundle): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a qualification bundle. */
data class QualificationBundleValidator(var rules: MutableList<QualificationBundleRule>) {
    fun validate(item: QualificationBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationBundleValidator(): QualificationBundleValidator {
    return QualificationBundleValidator(
        mutableListOf<QualificationBundleRule>(
            QualificationBundleRequiredContactEmailRule("qualification bundle needs an entry"),
            QualificationBundleRequiredCodeRule("qualification bundle needs a value"),
            QualificationBundleNameLengthRule(32),
            QualificationBundleContactEmailLengthRule(1000),
            QualificationBundleSegmentLengthRule(3600),
            QualificationBundleMarginRangeRule(2.7, 1239.5),
        ),
    )
}
