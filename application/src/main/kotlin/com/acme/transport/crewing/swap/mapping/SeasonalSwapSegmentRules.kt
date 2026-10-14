package com.acme.transport.crewing.swap.mapping

/** A rule a seasonal swap segment must pass. Check returns its problems, or none. */
interface SeasonalSwapSegmentRule {
    fun check(item: SeasonalSwapSegment): MutableList<String>
}

data class SeasonalSwapSegmentRequiredCodeRule(var message: String) : SeasonalSwapSegmentRule {
    override fun check(item: SeasonalSwapSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapSegmentRequiredShiftCodeRule(var message: String) : SeasonalSwapSegmentRule {
    override fun check(item: SeasonalSwapSegment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapSegmentRequiredNameRule(var message: String) : SeasonalSwapSegmentRule {
    override fun check(item: SeasonalSwapSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapSegmentNameLengthRule(var maxLength: Int) : SeasonalSwapSegmentRule {
    override fun check(item: SeasonalSwapSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal swap segment. */
data class SeasonalSwapSegmentValidator(var rules: MutableList<SeasonalSwapSegmentRule>) {
    fun validate(item: SeasonalSwapSegment): MutableList<String> {
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

    fun requireValid(item: SeasonalSwapSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSwapSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSwapSegmentValidator(): SeasonalSwapSegmentValidator {
    return SeasonalSwapSegmentValidator(
        mutableListOf<SeasonalSwapSegmentRule>(
            SeasonalSwapSegmentRequiredCodeRule("seasonal swap segment needs this field"),
            SeasonalSwapSegmentRequiredShiftCodeRule("seasonal swap segment needs this field"),
            SeasonalSwapSegmentRequiredNameRule("seasonal swap segment needs a value"),
            SeasonalSwapSegmentNameLengthRule(255),
        ),
    )
}
