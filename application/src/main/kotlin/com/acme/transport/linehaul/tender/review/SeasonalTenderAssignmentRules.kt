package com.acme.transport.linehaul.tender.review

/** A rule a seasonal tender assignment must pass. Check returns its problems, or none. */
interface SeasonalTenderAssignmentRule {
    fun check(item: SeasonalTenderAssignment): MutableList<String>
}

data class SeasonalTenderAssignmentCodePrefixRule(var prefix: String) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderAssignmentRequiredLabelRule(var message: String) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderAssignmentContactEmailLengthRule(var maxLength: Int) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderAssignmentDriverIdLengthRule(var maxLength: Int) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderAssignmentRequiredCodeRule(var message: String) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderAssignmentLabelLengthRule(var maxLength: Int) : SeasonalTenderAssignmentRule {
    override fun check(item: SeasonalTenderAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tender assignment. */
data class SeasonalTenderAssignmentValidator(var rules: MutableList<SeasonalTenderAssignmentRule>) {
    fun validate(item: SeasonalTenderAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTenderAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTenderAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTenderAssignmentValidator(): SeasonalTenderAssignmentValidator {
    return SeasonalTenderAssignmentValidator(
        mutableListOf<SeasonalTenderAssignmentRule>(
            SeasonalTenderAssignmentCodePrefixRule("OPS"),
            SeasonalTenderAssignmentRequiredLabelRule("seasonal tender assignment needs this field"),
            SeasonalTenderAssignmentContactEmailLengthRule(120),
            SeasonalTenderAssignmentDriverIdLengthRule(16),
            SeasonalTenderAssignmentRequiredCodeRule("seasonal tender assignment needs an entry"),
            SeasonalTenderAssignmentLabelLengthRule(16),
        ),
    )
}
