package com.acme.transport.freightpay.accessorial.accessorialpolicy

/** A rule a rolling accessorial policy must pass. Check returns its problems, or none. */
interface RollingAccessorialPolicyRule {
    fun check(item: RollingAccessorialPolicy): MutableList<String>
}

data class RollingAccessorialPolicyRequiredCodeRule(var message: String) : RollingAccessorialPolicyRule {
    override fun check(item: RollingAccessorialPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialPolicyCodeLengthRule(var maxLength: Int) : RollingAccessorialPolicyRule {
    override fun check(item: RollingAccessorialPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialPolicyDescriptionLengthRule(var maxLength: Int) : RollingAccessorialPolicyRule {
    override fun check(item: RollingAccessorialPolicy): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling accessorial policy. */
data class RollingAccessorialPolicyValidator(var rules: MutableList<RollingAccessorialPolicyRule>) {
    fun validate(item: RollingAccessorialPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAccessorialPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAccessorialPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAccessorialPolicyValidator(): RollingAccessorialPolicyValidator {
    return RollingAccessorialPolicyValidator(
        mutableListOf<RollingAccessorialPolicyRule>(
            RollingAccessorialPolicyRequiredCodeRule("rolling accessorial policy needs an entry"),
            RollingAccessorialPolicyCodeLengthRule(3600),
            RollingAccessorialPolicyDescriptionLengthRule(80),
        ),
    )
}
