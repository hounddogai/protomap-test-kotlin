package com.acme.transport.safety.cameraevent.ledger

/** A rule a weighted camera event ledger must pass. Check returns its problems, or none. */
interface WeightedCameraEventLedgerRule {
    fun check(item: WeightedCameraEventLedger): MutableList<String>
}

data class WeightedCameraEventLedgerQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCameraEventLedgerRule {
    override fun check(item: WeightedCameraEventLedger): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventLedgerLabelLengthRule(var maxLength: Int) : WeightedCameraEventLedgerRule {
    override fun check(item: WeightedCameraEventLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventLedgerCodeLengthRule(var maxLength: Int) : WeightedCameraEventLedgerRule {
    override fun check(item: WeightedCameraEventLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted camera event ledger. */
data class WeightedCameraEventLedgerValidator(var rules: MutableList<WeightedCameraEventLedgerRule>) {
    fun validate(item: WeightedCameraEventLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCameraEventLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCameraEventLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCameraEventLedgerValidator(): WeightedCameraEventLedgerValidator {
    return WeightedCameraEventLedgerValidator(
        mutableListOf<WeightedCameraEventLedgerRule>(
            WeightedCameraEventLedgerQuantityRangeRule(64, 80),
            WeightedCameraEventLedgerLabelLengthRule(3600),
            WeightedCameraEventLedgerCodeLengthRule(86400),
        ),
    )
}
