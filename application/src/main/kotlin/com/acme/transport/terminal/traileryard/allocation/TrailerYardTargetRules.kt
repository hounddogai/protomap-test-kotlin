package com.acme.transport.terminal.traileryard.allocation

/** A rule a trailer yard target must pass. Check returns its problems, or none. */
interface TrailerYardTargetRule {
    fun check(item: TrailerYardTarget): MutableList<String>
}

data class TrailerYardTargetLimitRangeRule(var min: Int, var max: Int) : TrailerYardTargetRule {
    override fun check(item: TrailerYardTarget): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class TrailerYardTargetOwnerIdLengthRule(var maxLength: Int) : TrailerYardTargetRule {
    override fun check(item: TrailerYardTarget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardTargetRequiredOwnerIdRule(var message: String) : TrailerYardTargetRule {
    override fun check(item: TrailerYardTarget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardTargetDistanceKmRangeRule(var min: Double, var max: Double) : TrailerYardTargetRule {
    override fun check(item: TrailerYardTarget): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardTargetCodePrefixRule(var prefix: String) : TrailerYardTargetRule {
    override fun check(item: TrailerYardTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard target. */
data class TrailerYardTargetValidator(var rules: MutableList<TrailerYardTargetRule>) {
    fun validate(item: TrailerYardTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardTargetValidator(): TrailerYardTargetValidator {
    return TrailerYardTargetValidator(
        mutableListOf<TrailerYardTargetRule>(
            TrailerYardTargetLimitRangeRule(80, 255),
            TrailerYardTargetOwnerIdLengthRule(1000),
            TrailerYardTargetRequiredOwnerIdRule("trailer yard target needs a value"),
            TrailerYardTargetDistanceKmRangeRule(5.9, 1775.6),
            TrailerYardTargetCodePrefixRule("Q"),
        ),
    )
}
