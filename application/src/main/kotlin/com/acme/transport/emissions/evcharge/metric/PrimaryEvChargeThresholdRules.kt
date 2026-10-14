package com.acme.transport.emissions.evcharge.metric

/** A rule a primary ev charge threshold must pass. Check returns its problems, or none. */
interface PrimaryEvChargeThresholdRule {
    fun check(item: PrimaryEvChargeThreshold): MutableList<String>
}

data class PrimaryEvChargeThresholdCodeLengthRule(var maxLength: Int) : PrimaryEvChargeThresholdRule {
    override fun check(item: PrimaryEvChargeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeThresholdUnitLengthRule(var maxLength: Int) : PrimaryEvChargeThresholdRule {
    override fun check(item: PrimaryEvChargeThreshold): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeThresholdRequiredUnitRule(var message: String) : PrimaryEvChargeThresholdRule {
    override fun check(item: PrimaryEvChargeThreshold): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEvChargeThresholdCodePrefixRule(var prefix: String) : PrimaryEvChargeThresholdRule {
    override fun check(item: PrimaryEvChargeThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary ev charge threshold. */
data class PrimaryEvChargeThresholdValidator(var rules: MutableList<PrimaryEvChargeThresholdRule>) {
    fun validate(item: PrimaryEvChargeThreshold): MutableList<String> {
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

    fun requireValid(item: PrimaryEvChargeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEvChargeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEvChargeThresholdValidator(): PrimaryEvChargeThresholdValidator {
    return PrimaryEvChargeThresholdValidator(
        mutableListOf<PrimaryEvChargeThresholdRule>(
            PrimaryEvChargeThresholdCodeLengthRule(80),
            PrimaryEvChargeThresholdUnitLengthRule(86400),
            PrimaryEvChargeThresholdRequiredUnitRule("primary ev charge threshold needs a value"),
            PrimaryEvChargeThresholdCodePrefixRule("REF"),
        ),
    )
}
