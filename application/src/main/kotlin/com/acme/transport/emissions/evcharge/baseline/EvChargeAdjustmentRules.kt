package com.acme.transport.emissions.evcharge.baseline

/** A rule a ev charge adjustment must pass. Check returns its problems, or none. */
interface EvChargeAdjustmentRule {
    fun check(item: EvChargeAdjustment): MutableList<String>
}

data class EvChargeAdjustmentRequiredReviewerRule(var message: String) : EvChargeAdjustmentRule {
    override fun check(item: EvChargeAdjustment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAdjustmentReviewerLengthRule(var maxLength: Int) : EvChargeAdjustmentRule {
    override fun check(item: EvChargeAdjustment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAdjustmentRequiredChecksumRule(var message: String) : EvChargeAdjustmentRule {
    override fun check(item: EvChargeAdjustment): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAdjustmentCodePrefixRule(var prefix: String) : EvChargeAdjustmentRule {
    override fun check(item: EvChargeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge adjustment. */
data class EvChargeAdjustmentValidator(var rules: MutableList<EvChargeAdjustmentRule>) {
    fun validate(item: EvChargeAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeAdjustmentValidator(): EvChargeAdjustmentValidator {
    return EvChargeAdjustmentValidator(
        mutableListOf<EvChargeAdjustmentRule>(
            EvChargeAdjustmentRequiredReviewerRule("ev charge adjustment needs an entry"),
            EvChargeAdjustmentReviewerLengthRule(64),
            EvChargeAdjustmentRequiredChecksumRule("ev charge adjustment needs a value"),
            EvChargeAdjustmentCodePrefixRule("X"),
        ),
    )
}
