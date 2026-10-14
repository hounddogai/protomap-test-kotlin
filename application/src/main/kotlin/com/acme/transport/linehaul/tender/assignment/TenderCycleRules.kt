package com.acme.transport.linehaul.tender.assignment

/** A rule a tender cycle must pass. Check returns its problems, or none. */
interface TenderCycleRule {
    fun check(item: TenderCycle): MutableList<String>
}

data class TenderCycleDistanceKmRangeRule(var min: Double, var max: Double) : TenderCycleRule {
    override fun check(item: TenderCycle): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class TenderCycleBatchSizeRangeRule(var min: Int, var max: Int) : TenderCycleRule {
    override fun check(item: TenderCycle): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderCycleCodeLengthRule(var maxLength: Int) : TenderCycleRule {
    override fun check(item: TenderCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderCycleCodePrefixRule(var prefix: String) : TenderCycleRule {
    override fun check(item: TenderCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderCycleSourceLengthRule(var maxLength: Int) : TenderCycleRule {
    override fun check(item: TenderCycle): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender cycle. */
data class TenderCycleValidator(var rules: MutableList<TenderCycleRule>) {
    fun validate(item: TenderCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderCycleValidator(): TenderCycleValidator {
    return TenderCycleValidator(
        mutableListOf<TenderCycleRule>(
            TenderCycleDistanceKmRangeRule(0.4, 1698.4),
            TenderCycleBatchSizeRangeRule(64, 80),
            TenderCycleCodeLengthRule(86400),
            TenderCycleCodePrefixRule("Q"),
            TenderCycleSourceLengthRule(120),
        ),
    )
}
