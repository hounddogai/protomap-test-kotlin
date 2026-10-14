package com.acme.transport.emissions.fuelburn.agreement

/** A rule a global fuel burn agreement must pass. Check returns its problems, or none. */
interface GlobalFuelBurnAgreementRule {
    fun check(item: GlobalFuelBurnAgreement): MutableList<String>
}

data class GlobalFuelBurnAgreementAttemptsRangeRule(var min: Int, var max: Int) : GlobalFuelBurnAgreementRule {
    override fun check(item: GlobalFuelBurnAgreement): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnAgreementCodeLengthRule(var maxLength: Int) : GlobalFuelBurnAgreementRule {
    override fun check(item: GlobalFuelBurnAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnAgreementRequiredLabelRule(var message: String) : GlobalFuelBurnAgreementRule {
    override fun check(item: GlobalFuelBurnAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnAgreementCodePrefixRule(var prefix: String) : GlobalFuelBurnAgreementRule {
    override fun check(item: GlobalFuelBurnAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel burn agreement. */
data class GlobalFuelBurnAgreementValidator(var rules: MutableList<GlobalFuelBurnAgreementRule>) {
    fun validate(item: GlobalFuelBurnAgreement): MutableList<String> {
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

    fun requireValid(item: GlobalFuelBurnAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelBurnAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelBurnAgreementValidator(): GlobalFuelBurnAgreementValidator {
    return GlobalFuelBurnAgreementValidator(
        mutableListOf<GlobalFuelBurnAgreementRule>(
            GlobalFuelBurnAgreementAttemptsRangeRule(32, 255),
            GlobalFuelBurnAgreementCodeLengthRule(86400),
            GlobalFuelBurnAgreementRequiredLabelRule("global fuel burn agreement needs a value"),
            GlobalFuelBurnAgreementCodePrefixRule("REF"),
        ),
    )
}
