package com.acme.transport.crewing.qualification.qualificationassignment

/** A rule a scheduled qualification threshold must pass. Check returns its problems, or none. */
interface ScheduledQualificationThresholdRule {
    fun check(item: ScheduledQualificationThreshold): MutableList<String>
}

data class ScheduledQualificationThresholdPinnedGuardRule(
    var limit: Int,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
        if (item.pinned && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationThresholdChecksumLengthRule(
    var maxLength: Int,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationThresholdVersionRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
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

data class ScheduledQualificationThresholdRequiredCodeRule(
    var message: String,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationThresholdRequiredChecksumRule(
    var message: String,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationThresholdNameLengthRule(
    var maxLength: Int,
) : ScheduledQualificationThresholdRule {
    override fun check(item: ScheduledQualificationThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled qualification threshold. */
data class ScheduledQualificationThresholdValidator(
    var rules: MutableList<ScheduledQualificationThresholdRule>,
) {
    fun validate(item: ScheduledQualificationThreshold): MutableList<String> {
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

    fun requireValid(item: ScheduledQualificationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledQualificationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledQualificationThresholdValidator(): ScheduledQualificationThresholdValidator {
    return ScheduledQualificationThresholdValidator(
        mutableListOf<ScheduledQualificationThresholdRule>(
            ScheduledQualificationThresholdPinnedGuardRule(32),
            ScheduledQualificationThresholdChecksumLengthRule(64),
            ScheduledQualificationThresholdVersionRangeRule(16, 1000),
            ScheduledQualificationThresholdRequiredCodeRule("scheduled qualification threshold needs a value"),
            ScheduledQualificationThresholdRequiredChecksumRule(
                "scheduled qualification threshold needs an entry",
            ),
            ScheduledQualificationThresholdNameLengthRule(16),
        ),
    )
}
