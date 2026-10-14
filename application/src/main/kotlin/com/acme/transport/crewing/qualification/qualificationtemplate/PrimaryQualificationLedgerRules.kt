package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.normalize

/** A rule a primary qualification ledger must pass. Check returns its problems, or none. */
interface PrimaryQualificationLedgerRule {
    fun check(item: PrimaryQualificationLedger): MutableList<String>
}

data class PrimaryQualificationLedgerQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryQualificationLedgerRule {
    override fun check(item: PrimaryQualificationLedger): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationLedgerRequiredTitleRule(var message: String) : PrimaryQualificationLedgerRule {
    override fun check(item: PrimaryQualificationLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryQualificationLedgerAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : PrimaryQualificationLedgerRule {
    override fun check(item: PrimaryQualificationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary qualification ledger. */
data class PrimaryQualificationLedgerValidator(var rules: MutableList<PrimaryQualificationLedgerRule>) {
    fun validate(item: PrimaryQualificationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryQualificationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryQualificationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryQualificationLedgerValidator(): PrimaryQualificationLedgerValidator {
    return PrimaryQualificationLedgerValidator(
        mutableListOf<PrimaryQualificationLedgerRule>(
            PrimaryQualificationLedgerQuantityRangeRule(120, 1000),
            PrimaryQualificationLedgerRequiredTitleRule("primary qualification ledger needs a value"),
            PrimaryQualificationLedgerAllowedVehicleIdRule(mutableListOf<String>("regional", "default")),
        ),
    )
}
