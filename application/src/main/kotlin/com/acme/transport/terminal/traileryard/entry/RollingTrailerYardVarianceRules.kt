package com.acme.transport.terminal.traileryard.entry

/** A rule a rolling trailer yard variance must pass. Check returns its problems, or none. */
interface RollingTrailerYardVarianceRule {
    fun check(item: RollingTrailerYardVariance): MutableList<String>
}

data class RollingTrailerYardVarianceNameLengthRule(var maxLength: Int) : RollingTrailerYardVarianceRule {
    override fun check(item: RollingTrailerYardVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardVarianceWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingTrailerYardVarianceRule {
    override fun check(item: RollingTrailerYardVariance): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardVarianceRequiredCountryRule(var message: String) : RollingTrailerYardVarianceRule {
    override fun check(item: RollingTrailerYardVariance): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardVarianceRequiredTrailerIdRule(
    var message: String,
) : RollingTrailerYardVarianceRule {
    override fun check(item: RollingTrailerYardVariance): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardVarianceCodeLengthRule(var maxLength: Int) : RollingTrailerYardVarianceRule {
    override fun check(item: RollingTrailerYardVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard variance. */
data class RollingTrailerYardVarianceValidator(var rules: MutableList<RollingTrailerYardVarianceRule>) {
    fun validate(item: RollingTrailerYardVariance): MutableList<String> {
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

    fun requireValid(item: RollingTrailerYardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardVarianceValidator(): RollingTrailerYardVarianceValidator {
    return RollingTrailerYardVarianceValidator(
        mutableListOf<RollingTrailerYardVarianceRule>(
            RollingTrailerYardVarianceNameLengthRule(120),
            RollingTrailerYardVarianceWeightRangeRule(2.6, 2740.6),
            RollingTrailerYardVarianceRequiredCountryRule("rolling trailer yard variance needs an entry"),
            RollingTrailerYardVarianceRequiredTrailerIdRule("rolling trailer yard variance needs an entry"),
            RollingTrailerYardVarianceCodeLengthRule(16),
        ),
    )
}
