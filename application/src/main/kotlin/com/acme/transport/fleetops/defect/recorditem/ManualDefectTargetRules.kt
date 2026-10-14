package com.acme.transport.fleetops.defect.recorditem

/** A rule a manual defect target must pass. Check returns its problems, or none. */
interface ManualDefectTargetRule {
    fun check(item: ManualDefectTarget): MutableList<String>
}

data class ManualDefectTargetScoreRangeRule(var min: Double, var max: Double) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectTargetLabelLengthRule(var maxLength: Int) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectTargetRequiredCodeRule(var message: String) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectTargetCodePrefixRule(var prefix: String) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectTargetRequiredLabelRule(var message: String) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectTargetActiveGuardRule(var limit: Int) : ManualDefectTargetRule {
    override fun check(item: ManualDefectTarget): MutableList<String> {
        if (item.active && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual defect target. */
data class ManualDefectTargetValidator(var rules: MutableList<ManualDefectTargetRule>) {
    fun validate(item: ManualDefectTarget): MutableList<String> {
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

    fun requireValid(item: ManualDefectTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDefectTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDefectTargetValidator(): ManualDefectTargetValidator {
    return ManualDefectTargetValidator(
        mutableListOf<ManualDefectTargetRule>(
            ManualDefectTargetScoreRangeRule(7.9, 3272.3),
            ManualDefectTargetLabelLengthRule(80),
            ManualDefectTargetRequiredCodeRule("manual defect target needs this field"),
            ManualDefectTargetCodePrefixRule("X"),
            ManualDefectTargetRequiredLabelRule("manual defect target needs this field"),
            ManualDefectTargetActiveGuardRule(1000),
        ),
    )
}
