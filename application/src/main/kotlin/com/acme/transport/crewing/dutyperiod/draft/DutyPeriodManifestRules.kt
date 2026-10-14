package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize

/** A rule a duty period manifest must pass. Check returns its problems, or none. */
interface DutyPeriodManifestRule {
    fun check(item: DutyPeriodManifest): MutableList<String>
}

data class DutyPeriodManifestAllowedSegmentRule(var allowed: MutableList<String>) : DutyPeriodManifestRule {
    override fun check(item: DutyPeriodManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodManifestRequiredCodeRule(var message: String) : DutyPeriodManifestRule {
    override fun check(item: DutyPeriodManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodManifestRequiredSegmentRule(var message: String) : DutyPeriodManifestRule {
    override fun check(item: DutyPeriodManifest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodManifestSegmentLengthRule(var maxLength: Int) : DutyPeriodManifestRule {
    override fun check(item: DutyPeriodManifest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodManifestQuantityRangeRule(var min: Int, var max: Int) : DutyPeriodManifestRule {
    override fun check(item: DutyPeriodManifest): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period manifest. */
data class DutyPeriodManifestValidator(var rules: MutableList<DutyPeriodManifestRule>) {
    fun validate(item: DutyPeriodManifest): MutableList<String> {
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

    fun requireValid(item: DutyPeriodManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodManifestValidator(): DutyPeriodManifestValidator {
    return DutyPeriodManifestValidator(
        mutableListOf<DutyPeriodManifestRule>(
            DutyPeriodManifestAllowedSegmentRule(mutableListOf<String>("adaptive")),
            DutyPeriodManifestRequiredCodeRule("duty period manifest needs a value"),
            DutyPeriodManifestRequiredSegmentRule("duty period manifest needs this field"),
            DutyPeriodManifestSegmentLengthRule(80),
            DutyPeriodManifestQuantityRangeRule(16, 32),
        ),
    )
}
