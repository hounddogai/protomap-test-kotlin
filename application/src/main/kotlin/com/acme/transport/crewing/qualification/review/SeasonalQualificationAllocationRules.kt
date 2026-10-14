package com.acme.transport.crewing.qualification.review

/** A rule a seasonal qualification allocation must pass. Check returns its problems, or none. */
interface SeasonalQualificationAllocationRule {
    fun check(item: SeasonalQualificationAllocation): MutableList<String>
}

data class SeasonalQualificationAllocationNameLengthRule(
    var maxLength: Int,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationAllocationCodeLengthRule(
    var maxLength: Int,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationAllocationRequiredCodeRule(
    var message: String,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationAllocationGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationAllocationStopCountRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class SeasonalQualificationAllocationRequiredDepotRule(
    var message: String,
) : SeasonalQualificationAllocationRule {
    override fun check(item: SeasonalQualificationAllocation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal qualification allocation. */
data class SeasonalQualificationAllocationValidator(
    var rules: MutableList<SeasonalQualificationAllocationRule>,
) {
    fun validate(item: SeasonalQualificationAllocation): MutableList<String> {
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

    fun requireValid(item: SeasonalQualificationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalQualificationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalQualificationAllocationValidator(): SeasonalQualificationAllocationValidator {
    return SeasonalQualificationAllocationValidator(
        mutableListOf<SeasonalQualificationAllocationRule>(
            SeasonalQualificationAllocationNameLengthRule(255),
            SeasonalQualificationAllocationCodeLengthRule(86400),
            SeasonalQualificationAllocationRequiredCodeRule("seasonal qualification allocation needs an entry"),
            SeasonalQualificationAllocationGraceDaysRangeRule(16, 80),
            SeasonalQualificationAllocationStopCountRangeRule(32, 64),
            SeasonalQualificationAllocationRequiredDepotRule(
                "seasonal qualification allocation needs this field",
            ),
        ),
    )
}
