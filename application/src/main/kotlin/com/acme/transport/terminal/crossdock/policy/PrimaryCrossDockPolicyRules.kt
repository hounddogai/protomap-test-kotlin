package com.acme.transport.terminal.crossdock.policy

/** A rule a primary cross dock policy must pass. Check returns its problems, or none. */
interface PrimaryCrossDockPolicyRule {
    fun check(item: PrimaryCrossDockPolicy): MutableList<String>
}

data class PrimaryCrossDockPolicyCodeLengthRule(var maxLength: Int) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockPolicyDepotLengthRule(var maxLength: Int) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockPolicyMarginRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockPolicyRatioRangeRule(var min: Double, var max: Double) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PrimaryCrossDockPolicyRequiredLabelRule(var message: String) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockPolicyRequiredCodeRule(var message: String) : PrimaryCrossDockPolicyRule {
    override fun check(item: PrimaryCrossDockPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cross dock policy. */
data class PrimaryCrossDockPolicyValidator(var rules: MutableList<PrimaryCrossDockPolicyRule>) {
    fun validate(item: PrimaryCrossDockPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCrossDockPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCrossDockPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCrossDockPolicyValidator(): PrimaryCrossDockPolicyValidator {
    return PrimaryCrossDockPolicyValidator(
        mutableListOf<PrimaryCrossDockPolicyRule>(
            PrimaryCrossDockPolicyCodeLengthRule(255),
            PrimaryCrossDockPolicyDepotLengthRule(255),
            PrimaryCrossDockPolicyMarginRangeRule(4.3, 1479.7),
            PrimaryCrossDockPolicyRatioRangeRule(1.5, 4068.0),
            PrimaryCrossDockPolicyRequiredLabelRule("primary cross dock policy needs this field"),
            PrimaryCrossDockPolicyRequiredCodeRule("primary cross dock policy needs an entry"),
        ),
    )
}
