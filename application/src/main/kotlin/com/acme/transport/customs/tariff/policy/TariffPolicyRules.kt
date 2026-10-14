package com.acme.transport.customs.tariff.policy

/** A rule a tariff policy must pass. Check returns its problems, or none. */
interface TariffPolicyRule {
    fun check(item: TariffPolicy): MutableList<String>
}

data class TariffPolicyLabelLengthRule(var maxLength: Int) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffPolicySequenceRangeRule(var min: Int, var max: Int) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class TariffPolicyOwnerIdLengthRule(var maxLength: Int) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffPolicyRequiredLabelRule(var message: String) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffPolicyRequiredCodeRule(var message: String) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffPolicyCodeLengthRule(var maxLength: Int) : TariffPolicyRule {
    override fun check(item: TariffPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff policy. */
data class TariffPolicyValidator(var rules: MutableList<TariffPolicyRule>) {
    fun validate(item: TariffPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffPolicyValidator(): TariffPolicyValidator {
    return TariffPolicyValidator(
        mutableListOf<TariffPolicyRule>(
            TariffPolicyLabelLengthRule(80),
            TariffPolicySequenceRangeRule(16, 16),
            TariffPolicyOwnerIdLengthRule(120),
            TariffPolicyRequiredLabelRule("tariff policy needs a value"),
            TariffPolicyRequiredCodeRule("tariff policy needs a value"),
            TariffPolicyCodeLengthRule(86400),
        ),
    )
}
