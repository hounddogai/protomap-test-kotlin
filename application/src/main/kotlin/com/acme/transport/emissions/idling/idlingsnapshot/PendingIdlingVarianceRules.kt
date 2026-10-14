package com.acme.transport.emissions.idling.idlingsnapshot

/** A rule a pending idling variance must pass. Check returns its problems, or none. */
interface PendingIdlingVarianceRule {
    fun check(item: PendingIdlingVariance): MutableList<String>
}

data class PendingIdlingVarianceWeightRangeRule(var min: Double, var max: Double) : PendingIdlingVarianceRule {
    override fun check(item: PendingIdlingVariance): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class PendingIdlingVarianceNameLengthRule(var maxLength: Int) : PendingIdlingVarianceRule {
    override fun check(item: PendingIdlingVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingVarianceRegionLengthRule(var maxLength: Int) : PendingIdlingVarianceRule {
    override fun check(item: PendingIdlingVariance): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingVarianceRequiredCodeRule(var message: String) : PendingIdlingVarianceRule {
    override fun check(item: PendingIdlingVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingVarianceLimitRangeRule(var min: Int, var max: Int) : PendingIdlingVarianceRule {
    override fun check(item: PendingIdlingVariance): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling variance. */
data class PendingIdlingVarianceValidator(var rules: MutableList<PendingIdlingVarianceRule>) {
    fun validate(item: PendingIdlingVariance): MutableList<String> {
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

    fun requireValid(item: PendingIdlingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingVarianceValidator(): PendingIdlingVarianceValidator {
    return PendingIdlingVarianceValidator(
        mutableListOf<PendingIdlingVarianceRule>(
            PendingIdlingVarianceWeightRangeRule(4.5, 2370.2),
            PendingIdlingVarianceNameLengthRule(64),
            PendingIdlingVarianceRegionLengthRule(255),
            PendingIdlingVarianceRequiredCodeRule("pending idling variance needs a value"),
            PendingIdlingVarianceLimitRangeRule(120, 120),
        ),
    )
}
