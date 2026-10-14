package com.acme.transport.customs.declaration.declarationsnapshot

/** A rule a seasonal declaration assignment must pass. Check returns its problems, or none. */
interface SeasonalDeclarationAssignmentRule {
    fun check(item: SeasonalDeclarationAssignment): MutableList<String>
}

data class SeasonalDeclarationAssignmentOdometerRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationAssignmentCodeLengthRule(var maxLength: Int) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationAssignmentContactEmailLengthRule(
    var maxLength: Int,
) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationAssignmentCodePrefixRule(var prefix: String) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationAssignmentNameLengthRule(var maxLength: Int) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationAssignmentWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDeclarationAssignmentRule {
    override fun check(item: SeasonalDeclarationAssignment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal declaration assignment. */
data class SeasonalDeclarationAssignmentValidator(var rules: MutableList<SeasonalDeclarationAssignmentRule>) {
    fun validate(item: SeasonalDeclarationAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalDeclarationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDeclarationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDeclarationAssignmentValidator(): SeasonalDeclarationAssignmentValidator {
    return SeasonalDeclarationAssignmentValidator(
        mutableListOf<SeasonalDeclarationAssignmentRule>(
            SeasonalDeclarationAssignmentOdometerRangeRule(120, 255),
            SeasonalDeclarationAssignmentCodeLengthRule(64),
            SeasonalDeclarationAssignmentContactEmailLengthRule(32),
            SeasonalDeclarationAssignmentCodePrefixRule("Q"),
            SeasonalDeclarationAssignmentNameLengthRule(1000),
            SeasonalDeclarationAssignmentWindowDaysRangeRule(80, 1000),
        ),
    )
}
