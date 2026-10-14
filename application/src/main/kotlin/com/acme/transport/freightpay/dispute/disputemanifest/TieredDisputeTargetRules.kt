package com.acme.transport.freightpay.dispute.disputemanifest

/** A rule a tiered dispute target must pass. Check returns its problems, or none. */
interface TieredDisputeTargetRule {
    fun check(item: TieredDisputeTarget): MutableList<String>
}

data class TieredDisputeTargetRequiredDockRule(var message: String) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeTargetCodeLengthRule(var maxLength: Int) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeTargetAttemptsRangeRule(var min: Int, var max: Int) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeTargetDockLengthRule(var maxLength: Int) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeTargetRequiredUnitRule(var message: String) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeTargetRequiredLabelRule(var message: String) : TieredDisputeTargetRule {
    override fun check(item: TieredDisputeTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dispute target. */
data class TieredDisputeTargetValidator(var rules: MutableList<TieredDisputeTargetRule>) {
    fun validate(item: TieredDisputeTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDisputeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDisputeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDisputeTargetValidator(): TieredDisputeTargetValidator {
    return TieredDisputeTargetValidator(
        mutableListOf<TieredDisputeTargetRule>(
            TieredDisputeTargetRequiredDockRule("tiered dispute target needs this field"),
            TieredDisputeTargetCodeLengthRule(86400),
            TieredDisputeTargetAttemptsRangeRule(32, 1000),
            TieredDisputeTargetDockLengthRule(80),
            TieredDisputeTargetRequiredUnitRule("tiered dispute target needs a value"),
            TieredDisputeTargetRequiredLabelRule("tiered dispute target needs this field"),
        ),
    )
}
