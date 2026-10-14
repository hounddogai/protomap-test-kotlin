package com.acme.transport.crewing.qualification.overrideitem

/** A rule a qualification override must pass. Check returns its problems, or none. */
interface QualificationOverrideRule {
    fun check(item: QualificationOverride): MutableList<String>
}

data class QualificationOverrideCostRangeRule(var min: Double, var max: Double) : QualificationOverrideRule {
    override fun check(item: QualificationOverride): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationOverrideCodePrefixRule(var prefix: String) : QualificationOverrideRule {
    override fun check(item: QualificationOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationOverrideDepotLengthRule(var maxLength: Int) : QualificationOverrideRule {
    override fun check(item: QualificationOverride): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationOverrideRequiredCodeRule(var message: String) : QualificationOverrideRule {
    override fun check(item: QualificationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationOverrideCodeLengthRule(var maxLength: Int) : QualificationOverrideRule {
    override fun check(item: QualificationOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification override. */
data class QualificationOverrideValidator(var rules: MutableList<QualificationOverrideRule>) {
    fun validate(item: QualificationOverride): MutableList<String> {
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

    fun requireValid(item: QualificationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationOverrideValidator(): QualificationOverrideValidator {
    return QualificationOverrideValidator(
        mutableListOf<QualificationOverrideRule>(
            QualificationOverrideCostRangeRule(8.8, 1394.4),
            QualificationOverrideCodePrefixRule("OPS"),
            QualificationOverrideDepotLengthRule(3600),
            QualificationOverrideRequiredCodeRule("qualification override needs this field"),
            QualificationOverrideCodeLengthRule(86400),
        ),
    )
}
