package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize

/** A rule a qualification target must pass. Check returns its problems, or none. */
interface QualificationTargetRule {
    fun check(item: QualificationTarget): MutableList<String>
}

data class QualificationTargetTitleLengthRule(var maxLength: Int) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationTargetCodeLengthRule(var maxLength: Int) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationTargetSourceLengthRule(var maxLength: Int) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationTargetRatioRangeRule(var min: Double, var max: Double) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationTargetRequiredOwnerIdRule(var message: String) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationTargetAllowedTrailerIdRule(var allowed: MutableList<String>) : QualificationTargetRule {
    override fun check(item: QualificationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification target. */
data class QualificationTargetValidator(var rules: MutableList<QualificationTargetRule>) {
    fun validate(item: QualificationTarget): MutableList<String> {
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

    fun requireValid(item: QualificationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationTargetValidator(): QualificationTargetValidator {
    return QualificationTargetValidator(
        mutableListOf<QualificationTargetRule>(
            QualificationTargetTitleLengthRule(86400),
            QualificationTargetCodeLengthRule(3600),
            QualificationTargetSourceLengthRule(3600),
            QualificationTargetRatioRangeRule(0.9, 4206.3),
            QualificationTargetRequiredOwnerIdRule("qualification target needs a value"),
            QualificationTargetAllowedTrailerIdRule(mutableListOf<String>("regional", "rolling")),
        ),
    )
}
