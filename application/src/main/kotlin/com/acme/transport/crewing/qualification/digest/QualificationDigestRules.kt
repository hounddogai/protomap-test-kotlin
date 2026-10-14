package com.acme.transport.crewing.qualification.digest

/** A rule a qualification digest must pass. Check returns its problems, or none. */
interface QualificationDigestRule {
    fun check(item: QualificationDigest): MutableList<String>
}

data class QualificationDigestRequiredCodeRule(var message: String) : QualificationDigestRule {
    override fun check(item: QualificationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationDigestRegionLengthRule(var maxLength: Int) : QualificationDigestRule {
    override fun check(item: QualificationDigest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationDigestRequiredRouteCodeRule(var message: String) : QualificationDigestRule {
    override fun check(item: QualificationDigest): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationDigestRequiredRegionRule(var message: String) : QualificationDigestRule {
    override fun check(item: QualificationDigest): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationDigestVersionRangeRule(var min: Int, var max: Int) : QualificationDigestRule {
    override fun check(item: QualificationDigest): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a qualification digest. */
data class QualificationDigestValidator(var rules: MutableList<QualificationDigestRule>) {
    fun validate(item: QualificationDigest): MutableList<String> {
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

    fun requireValid(item: QualificationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationDigestValidator(): QualificationDigestValidator {
    return QualificationDigestValidator(
        mutableListOf<QualificationDigestRule>(
            QualificationDigestRequiredCodeRule("qualification digest needs this field"),
            QualificationDigestRegionLengthRule(86400),
            QualificationDigestRequiredRouteCodeRule("qualification digest needs a value"),
            QualificationDigestRequiredRegionRule("qualification digest needs a value"),
            QualificationDigestVersionRangeRule(64, 80),
        ),
    )
}
