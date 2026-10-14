package com.acme.transport.crewing.qualification.cycle

/** A rule a qualification segment must pass. Check returns its problems, or none. */
interface QualificationSegmentRule {
    fun check(item: QualificationSegment): MutableList<String>
}

data class QualificationSegmentLaneLengthRule(var maxLength: Int) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSegmentCodeLengthRule(var maxLength: Int) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSegmentPinnedGuardRule(var limit: Int) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        if (item.pinned && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSegmentRequiredRouteCodeRule(var message: String) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationSegmentRouteCodeLengthRule(var maxLength: Int) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSegmentCodePrefixRule(var prefix: String) : QualificationSegmentRule {
    override fun check(item: QualificationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification segment. */
data class QualificationSegmentValidator(var rules: MutableList<QualificationSegmentRule>) {
    fun validate(item: QualificationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationSegmentValidator(): QualificationSegmentValidator {
    return QualificationSegmentValidator(
        mutableListOf<QualificationSegmentRule>(
            QualificationSegmentLaneLengthRule(16),
            QualificationSegmentCodeLengthRule(120),
            QualificationSegmentPinnedGuardRule(120),
            QualificationSegmentRequiredRouteCodeRule("qualification segment needs an entry"),
            QualificationSegmentRouteCodeLengthRule(64),
            QualificationSegmentCodePrefixRule("Q"),
        ),
    )
}
