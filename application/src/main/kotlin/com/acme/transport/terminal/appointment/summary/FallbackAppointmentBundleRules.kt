package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize

/** A rule a fallback appointment bundle must pass. Check returns its problems, or none. */
interface FallbackAppointmentBundleRule {
    fun check(item: FallbackAppointmentBundle): MutableList<String>
}

data class FallbackAppointmentBundleCodeLengthRule(var maxLength: Int) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBundleRequiredCodeRule(var message: String) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBundleSegmentLengthRule(var maxLength: Int) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBundleAllowedTitleRule(
    var allowed: MutableList<String>,
) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBundleApprovedGuardRule(var limit: Double) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        if (item.approved && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBundleRequiredNotesRule(var message: String) : FallbackAppointmentBundleRule {
    override fun check(item: FallbackAppointmentBundle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment bundle. */
data class FallbackAppointmentBundleValidator(var rules: MutableList<FallbackAppointmentBundleRule>) {
    fun validate(item: FallbackAppointmentBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAppointmentBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentBundleValidator(): FallbackAppointmentBundleValidator {
    return FallbackAppointmentBundleValidator(
        mutableListOf<FallbackAppointmentBundleRule>(
            FallbackAppointmentBundleCodeLengthRule(64),
            FallbackAppointmentBundleRequiredCodeRule("fallback appointment bundle needs an entry"),
            FallbackAppointmentBundleSegmentLengthRule(80),
            FallbackAppointmentBundleAllowedTitleRule(mutableListOf<String>("manual")),
            FallbackAppointmentBundleApprovedGuardRule(0.6),
            FallbackAppointmentBundleRequiredNotesRule("fallback appointment bundle needs this field"),
        ),
    )
}
