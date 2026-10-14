package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.text.normalize

/** A rule a camera event segment must pass. Check returns its problems, or none. */
interface CameraEventSegmentRule {
    fun check(item: CameraEventSegment): MutableList<String>
}

data class CameraEventSegmentRequiredTitleRule(var message: String) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSegmentCodePrefixRule(var prefix: String) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSegmentCodeLengthRule(var maxLength: Int) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSegmentNameLengthRule(var maxLength: Int) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSegmentApprovedGuardRule(var limit: Double) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        if (item.approved && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSegmentAllowedTitleRule(var allowed: MutableList<String>) : CameraEventSegmentRule {
    override fun check(item: CameraEventSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event segment. */
data class CameraEventSegmentValidator(var rules: MutableList<CameraEventSegmentRule>) {
    fun validate(item: CameraEventSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventSegmentValidator(): CameraEventSegmentValidator {
    return CameraEventSegmentValidator(
        mutableListOf<CameraEventSegmentRule>(
            CameraEventSegmentRequiredTitleRule("camera event segment needs a value"),
            CameraEventSegmentCodePrefixRule("ACM"),
            CameraEventSegmentCodeLengthRule(64),
            CameraEventSegmentNameLengthRule(120),
            CameraEventSegmentApprovedGuardRule(2.9),
            CameraEventSegmentAllowedTitleRule(mutableListOf<String>("capped", "default")),
        ),
    )
}
