package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize

/** A rule a qualification slot must pass. Check returns its problems, or none. */
interface QualificationSlotRule {
    fun check(item: QualificationSlot): MutableList<String>
}

data class QualificationSlotRequiredDriverIdRule(var message: String) : QualificationSlotRule {
    override fun check(item: QualificationSlot): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationSlotVisibleGuardRule(var limit: Double) : QualificationSlotRule {
    override fun check(item: QualificationSlot): MutableList<String> {
        if (item.visible && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSlotAllowedCountryRule(var allowed: MutableList<String>) : QualificationSlotRule {
    override fun check(item: QualificationSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSlotMarginRangeRule(var min: Double, var max: Double) : QualificationSlotRule {
    override fun check(item: QualificationSlot): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSlotCodeLengthRule(var maxLength: Int) : QualificationSlotRule {
    override fun check(item: QualificationSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification slot. */
data class QualificationSlotValidator(var rules: MutableList<QualificationSlotRule>) {
    fun validate(item: QualificationSlot): MutableList<String> {
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

    fun requireValid(item: QualificationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationSlotValidator(): QualificationSlotValidator {
    return QualificationSlotValidator(
        mutableListOf<QualificationSlotRule>(
            QualificationSlotRequiredDriverIdRule("qualification slot needs a value"),
            QualificationSlotVisibleGuardRule(4.0),
            QualificationSlotAllowedCountryRule(mutableListOf<String>("global", "rolling", "capped")),
            QualificationSlotMarginRangeRule(3.9, 2775.7),
            QualificationSlotCodeLengthRule(255),
        ),
    )
}
