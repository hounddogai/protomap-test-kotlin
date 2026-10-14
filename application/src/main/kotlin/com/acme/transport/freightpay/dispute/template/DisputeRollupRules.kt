package com.acme.transport.freightpay.dispute.template

/** A rule a dispute rollup must pass. Check returns its problems, or none. */
interface DisputeRollupRule {
    fun check(item: DisputeRollup): MutableList<String>
}

data class DisputeRollupCodeLengthRule(var maxLength: Int) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRollupRequiredTitleRule(var message: String) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeRollupVehicleIdLengthRule(var maxLength: Int) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRollupRequiredCodeRule(var message: String) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeRollupRequiredLabelRule(var message: String) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeRollupRequiredVehicleIdRule(var message: String) : DisputeRollupRule {
    override fun check(item: DisputeRollup): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute rollup. */
data class DisputeRollupValidator(var rules: MutableList<DisputeRollupRule>) {
    fun validate(item: DisputeRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeRollupValidator(): DisputeRollupValidator {
    return DisputeRollupValidator(
        mutableListOf<DisputeRollupRule>(
            DisputeRollupCodeLengthRule(32),
            DisputeRollupRequiredTitleRule("dispute rollup needs this field"),
            DisputeRollupVehicleIdLengthRule(16),
            DisputeRollupRequiredCodeRule("dispute rollup needs an entry"),
            DisputeRollupRequiredLabelRule("dispute rollup needs this field"),
            DisputeRollupRequiredVehicleIdRule("dispute rollup needs this field"),
        ),
    )
}
