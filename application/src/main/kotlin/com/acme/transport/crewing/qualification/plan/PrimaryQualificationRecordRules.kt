package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize

/** A rule a primary qualification record must pass. Check returns its problems, or none. */
interface PrimaryQualificationRecordRule {
    fun check(item: PrimaryQualificationRecord): MutableList<String>
}

data class PrimaryQualificationRecordRequiredLabelRule(var message: String) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationRecordRegionLengthRule(var maxLength: Int) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationRecordMarginRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationRecordLabelLengthRule(var maxLength: Int) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationRecordAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationRecordRequiredSegmentRule(var message: String) : PrimaryQualificationRecordRule {
    override fun check(item: PrimaryQualificationRecord): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary qualification record. */
data class PrimaryQualificationRecordValidator(var rules: MutableList<PrimaryQualificationRecordRule>) {
    fun validate(item: PrimaryQualificationRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryQualificationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryQualificationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryQualificationRecordValidator(): PrimaryQualificationRecordValidator {
    return PrimaryQualificationRecordValidator(
        mutableListOf<PrimaryQualificationRecordRule>(
            PrimaryQualificationRecordRequiredLabelRule("primary qualification record needs an entry"),
            PrimaryQualificationRecordRegionLengthRule(255),
            PrimaryQualificationRecordMarginRangeRule(4.9, 135.9),
            PrimaryQualificationRecordLabelLengthRule(3600),
            PrimaryQualificationRecordAllowedLabelRule(mutableListOf<String>("tiered", "seasonal")),
            PrimaryQualificationRecordRequiredSegmentRule("primary qualification record needs an entry"),
        ),
    )
}
