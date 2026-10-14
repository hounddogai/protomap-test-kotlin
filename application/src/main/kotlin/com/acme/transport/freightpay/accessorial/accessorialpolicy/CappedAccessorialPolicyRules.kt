package com.acme.transport.freightpay.accessorial.accessorialpolicy

/** A rule a capped accessorial policy must pass. Check returns its problems, or none. */
interface CappedAccessorialPolicyRule {
    fun check(item: CappedAccessorialPolicy): MutableList<String>
}

data class CappedAccessorialPolicyAttemptsRangeRule(var min: Int, var max: Int) : CappedAccessorialPolicyRule {
    override fun check(item: CappedAccessorialPolicy): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class CappedAccessorialPolicyRequiredNameRule(var message: String) : CappedAccessorialPolicyRule {
    override fun check(item: CappedAccessorialPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialPolicyCodeLengthRule(var maxLength: Int) : CappedAccessorialPolicyRule {
    override fun check(item: CappedAccessorialPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialPolicyNameLengthRule(var maxLength: Int) : CappedAccessorialPolicyRule {
    override fun check(item: CappedAccessorialPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped accessorial policy. */
data class CappedAccessorialPolicyValidator(var rules: MutableList<CappedAccessorialPolicyRule>) {
    fun validate(item: CappedAccessorialPolicy): MutableList<String> {
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

    fun requireValid(item: CappedAccessorialPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAccessorialPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAccessorialPolicyValidator(): CappedAccessorialPolicyValidator {
    return CappedAccessorialPolicyValidator(
        mutableListOf<CappedAccessorialPolicyRule>(
            CappedAccessorialPolicyAttemptsRangeRule(16, 80),
            CappedAccessorialPolicyRequiredNameRule("capped accessorial policy needs this field"),
            CappedAccessorialPolicyCodeLengthRule(1000),
            CappedAccessorialPolicyNameLengthRule(86400),
        ),
    )
}
