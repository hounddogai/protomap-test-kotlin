package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize

/** A rule a default parcel assignment must pass. Check returns its problems, or none. */
interface DefaultParcelAssignmentRule {
    fun check(item: DefaultParcelAssignment): MutableList<String>
}

data class DefaultParcelAssignmentRequiredNameRule(var message: String) : DefaultParcelAssignmentRule {
    override fun check(item: DefaultParcelAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelAssignmentAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : DefaultParcelAssignmentRule {
    override fun check(item: DefaultParcelAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelAssignmentCostRangeRule(
    var min: Double,
    var max: Double,
) : DefaultParcelAssignmentRule {
    override fun check(item: DefaultParcelAssignment): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DefaultParcelAssignmentCodePrefixRule(var prefix: String) : DefaultParcelAssignmentRule {
    override fun check(item: DefaultParcelAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelAssignmentCodeLengthRule(var maxLength: Int) : DefaultParcelAssignmentRule {
    override fun check(item: DefaultParcelAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel assignment. */
data class DefaultParcelAssignmentValidator(var rules: MutableList<DefaultParcelAssignmentRule>) {
    fun validate(item: DefaultParcelAssignment): MutableList<String> {
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

    fun requireValid(item: DefaultParcelAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelAssignmentValidator(): DefaultParcelAssignmentValidator {
    return DefaultParcelAssignmentValidator(
        mutableListOf<DefaultParcelAssignmentRule>(
            DefaultParcelAssignmentRequiredNameRule("default parcel assignment needs an entry"),
            DefaultParcelAssignmentAllowedDriverIdRule(mutableListOf<String>("fallback")),
            DefaultParcelAssignmentCostRangeRule(1.6, 2744.7),
            DefaultParcelAssignmentCodePrefixRule("ACM"),
            DefaultParcelAssignmentCodeLengthRule(16),
        ),
    )
}
