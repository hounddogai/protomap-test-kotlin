package com.acme.transport.fleetops.inspection.profile

/** A rule a lenient inspection window must pass. Check returns its problems, or none. */
interface LenientInspectionWindowRule {
    fun check(item: LenientInspectionWindow): MutableList<String>
}

data class LenientInspectionWindowArchivedGuardRule(var limit: Int) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        if (item.archived && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionWindowRegionLengthRule(var maxLength: Int) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionWindowRequiredCodeRule(var message: String) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionWindowRequiredRegionRule(var message: String) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionWindowCodePrefixRule(var prefix: String) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionWindowLabelLengthRule(var maxLength: Int) : LenientInspectionWindowRule {
    override fun check(item: LenientInspectionWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient inspection window. */
data class LenientInspectionWindowValidator(var rules: MutableList<LenientInspectionWindowRule>) {
    fun validate(item: LenientInspectionWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientInspectionWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientInspectionWindowValidator(): LenientInspectionWindowValidator {
    return LenientInspectionWindowValidator(
        mutableListOf<LenientInspectionWindowRule>(
            LenientInspectionWindowArchivedGuardRule(16),
            LenientInspectionWindowRegionLengthRule(120),
            LenientInspectionWindowRequiredCodeRule("lenient inspection window needs an entry"),
            LenientInspectionWindowRequiredRegionRule("lenient inspection window needs a value"),
            LenientInspectionWindowCodePrefixRule("REF"),
            LenientInspectionWindowLabelLengthRule(255),
        ),
    )
}
