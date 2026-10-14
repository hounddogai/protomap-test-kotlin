package com.acme.transport.linehaul.capacity.ledger

/** A rule a seasonal capacity estimate must pass. Check returns its problems, or none. */
interface SeasonalCapacityEstimateRule {
    fun check(item: SeasonalCapacityEstimate): MutableList<String>
}

data class SeasonalCapacityEstimateNameLengthRule(var maxLength: Int) : SeasonalCapacityEstimateRule {
    override fun check(item: SeasonalCapacityEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityEstimateCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalCapacityEstimateRule {
    override fun check(item: SeasonalCapacityEstimate): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityEstimateCodeLengthRule(var maxLength: Int) : SeasonalCapacityEstimateRule {
    override fun check(item: SeasonalCapacityEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityEstimateChecksumLengthRule(var maxLength: Int) : SeasonalCapacityEstimateRule {
    override fun check(item: SeasonalCapacityEstimate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal capacity estimate. */
data class SeasonalCapacityEstimateValidator(var rules: MutableList<SeasonalCapacityEstimateRule>) {
    fun validate(item: SeasonalCapacityEstimate): MutableList<String> {
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

    fun requireValid(item: SeasonalCapacityEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacityEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacityEstimateValidator(): SeasonalCapacityEstimateValidator {
    return SeasonalCapacityEstimateValidator(
        mutableListOf<SeasonalCapacityEstimateRule>(
            SeasonalCapacityEstimateNameLengthRule(1000),
            SeasonalCapacityEstimateCreatedAtRangeRule(1000, 3600),
            SeasonalCapacityEstimateCodeLengthRule(16),
            SeasonalCapacityEstimateChecksumLengthRule(32),
        ),
    )
}
