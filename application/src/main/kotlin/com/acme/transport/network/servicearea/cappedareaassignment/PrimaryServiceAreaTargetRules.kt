package com.acme.transport.network.servicearea.cappedareaassignment

/** A rule a primary service area target must pass. Check returns its problems, or none. */
interface PrimaryServiceAreaTargetRule {
    fun check(item: PrimaryServiceAreaTarget): MutableList<String>
}

data class PrimaryServiceAreaTargetCodePrefixRule(var prefix: String) : PrimaryServiceAreaTargetRule {
    override fun check(item: PrimaryServiceAreaTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaTargetCodeLengthRule(var maxLength: Int) : PrimaryServiceAreaTargetRule {
    override fun check(item: PrimaryServiceAreaTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaTargetRequiredTrailerIdRule(var message: String) : PrimaryServiceAreaTargetRule {
    override fun check(item: PrimaryServiceAreaTarget): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaTargetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServiceAreaTargetRule {
    override fun check(item: PrimaryServiceAreaTarget): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service area target. */
data class PrimaryServiceAreaTargetValidator(var rules: MutableList<PrimaryServiceAreaTargetRule>) {
    fun validate(item: PrimaryServiceAreaTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServiceAreaTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServiceAreaTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServiceAreaTargetValidator(): PrimaryServiceAreaTargetValidator {
    return PrimaryServiceAreaTargetValidator(
        mutableListOf<PrimaryServiceAreaTargetRule>(
            PrimaryServiceAreaTargetCodePrefixRule("ACM"),
            PrimaryServiceAreaTargetCodeLengthRule(255),
            PrimaryServiceAreaTargetRequiredTrailerIdRule("primary service area target needs this field"),
            PrimaryServiceAreaTargetThresholdRangeRule(4.0, 2025.3),
        ),
    )
}
