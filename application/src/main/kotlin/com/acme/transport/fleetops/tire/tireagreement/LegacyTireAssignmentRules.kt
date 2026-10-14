package com.acme.transport.fleetops.tire.tireagreement

/** A rule a legacy tire assignment must pass. Check returns its problems, or none. */
interface LegacyTireAssignmentRule {
    fun check(item: LegacyTireAssignment): MutableList<String>
}

data class LegacyTireAssignmentRequiredCodeRule(var message: String) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireAssignmentCodeLengthRule(var maxLength: Int) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireAssignmentTitleLengthRule(var maxLength: Int) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireAssignmentSegmentLengthRule(var maxLength: Int) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireAssignmentCodePrefixRule(var prefix: String) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireAssignmentEtaMinutesRangeRule(var min: Int, var max: Int) : LegacyTireAssignmentRule {
    override fun check(item: LegacyTireAssignment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy tire assignment. */
data class LegacyTireAssignmentValidator(var rules: MutableList<LegacyTireAssignmentRule>) {
    fun validate(item: LegacyTireAssignment): MutableList<String> {
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

    fun requireValid(item: LegacyTireAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireAssignmentValidator(): LegacyTireAssignmentValidator {
    return LegacyTireAssignmentValidator(
        mutableListOf<LegacyTireAssignmentRule>(
            LegacyTireAssignmentRequiredCodeRule("legacy tire assignment needs an entry"),
            LegacyTireAssignmentCodeLengthRule(16),
            LegacyTireAssignmentTitleLengthRule(3600),
            LegacyTireAssignmentSegmentLengthRule(32),
            LegacyTireAssignmentCodePrefixRule("ACM"),
            LegacyTireAssignmentEtaMinutesRangeRule(80, 120),
        ),
    )
}
