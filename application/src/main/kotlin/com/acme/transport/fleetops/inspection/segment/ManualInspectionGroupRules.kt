package com.acme.transport.fleetops.inspection.segment

/** A rule a manual inspection group must pass. Check returns its problems, or none. */
interface ManualInspectionGroupRule {
    fun check(item: ManualInspectionGroup): MutableList<String>
}

data class ManualInspectionGroupLoadKgRangeRule(var min: Double, var max: Double) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupCodePrefixRule(var prefix: String) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupCodeLengthRule(var maxLength: Int) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupRequiredCodeRule(var message: String) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupRequiredNameRule(var message: String) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual inspection group. */
data class ManualInspectionGroupValidator(var rules: MutableList<ManualInspectionGroupRule>) {
    fun validate(item: ManualInspectionGroup): MutableList<String> {
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

    fun requireValid(item: ManualInspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionGroupValidator(): ManualInspectionGroupValidator {
    return ManualInspectionGroupValidator(
        mutableListOf<ManualInspectionGroupRule>(
            ManualInspectionGroupLoadKgRangeRule(3.2, 2061.6),
            ManualInspectionGroupCodePrefixRule("X"),
            ManualInspectionGroupCodeLengthRule(3600),
            ManualInspectionGroupRequiredCodeRule("manual inspection group needs an entry"),
            ManualInspectionGroupRequiredNameRule("manual inspection group needs this field"),
        ),
    )
}
