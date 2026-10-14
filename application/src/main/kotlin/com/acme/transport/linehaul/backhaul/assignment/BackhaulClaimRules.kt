package com.acme.transport.linehaul.backhaul.assignment

/** A rule a backhaul claim must pass. Check returns its problems, or none. */
interface BackhaulClaimRule {
    fun check(item: BackhaulClaim): MutableList<String>
}

data class BackhaulClaimRequiredDescriptionRule(var message: String) : BackhaulClaimRule {
    override fun check(item: BackhaulClaim): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulClaimNameLengthRule(var maxLength: Int) : BackhaulClaimRule {
    override fun check(item: BackhaulClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulClaimCodeLengthRule(var maxLength: Int) : BackhaulClaimRule {
    override fun check(item: BackhaulClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulClaimCodePrefixRule(var prefix: String) : BackhaulClaimRule {
    override fun check(item: BackhaulClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul claim. */
data class BackhaulClaimValidator(var rules: MutableList<BackhaulClaimRule>) {
    fun validate(item: BackhaulClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulClaimValidator(): BackhaulClaimValidator {
    return BackhaulClaimValidator(
        mutableListOf<BackhaulClaimRule>(
            BackhaulClaimRequiredDescriptionRule("backhaul claim needs a value"),
            BackhaulClaimNameLengthRule(120),
            BackhaulClaimCodeLengthRule(86400),
            BackhaulClaimCodePrefixRule("OPS"),
        ),
    )
}
