package com.acme.transport.terminal.traileryard.yardentry

/** A rule a rolling trailer yard policy must pass. Check returns its problems, or none. */
interface RollingTrailerYardPolicyRule {
    fun check(item: RollingTrailerYardPolicy): MutableList<String>
}

data class RollingTrailerYardPolicyRequiredTitleRule(var message: String) : RollingTrailerYardPolicyRule {
    override fun check(item: RollingTrailerYardPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardPolicyAmountRangeRule(
    var min: Double,
    var max: Double,
) : RollingTrailerYardPolicyRule {
    override fun check(item: RollingTrailerYardPolicy): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RollingTrailerYardPolicyNameLengthRule(var maxLength: Int) : RollingTrailerYardPolicyRule {
    override fun check(item: RollingTrailerYardPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardPolicyCeilingRangeRule(
    var min: Double,
    var max: Double,
) : RollingTrailerYardPolicyRule {
    override fun check(item: RollingTrailerYardPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class RollingTrailerYardPolicyRequiredChecksumRule(var message: String) : RollingTrailerYardPolicyRule {
    override fun check(item: RollingTrailerYardPolicy): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard policy. */
data class RollingTrailerYardPolicyValidator(var rules: MutableList<RollingTrailerYardPolicyRule>) {
    fun validate(item: RollingTrailerYardPolicy): MutableList<String> {
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

    fun requireValid(item: RollingTrailerYardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardPolicyValidator(): RollingTrailerYardPolicyValidator {
    return RollingTrailerYardPolicyValidator(
        mutableListOf<RollingTrailerYardPolicyRule>(
            RollingTrailerYardPolicyRequiredTitleRule("rolling trailer yard policy needs a value"),
            RollingTrailerYardPolicyAmountRangeRule(3.5, 1369.9),
            RollingTrailerYardPolicyNameLengthRule(16),
            RollingTrailerYardPolicyCeilingRangeRule(1.8, 1213.2),
            RollingTrailerYardPolicyRequiredChecksumRule("rolling trailer yard policy needs a value"),
        ),
    )
}
