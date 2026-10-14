package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize

/** A rule a rolling tariff batch must pass. Check returns its problems, or none. */
interface RollingTariffBatchRule {
    fun check(item: RollingTariffBatch): MutableList<String>
}

data class RollingTariffBatchRequiredNotesRule(var message: String) : RollingTariffBatchRule {
    override fun check(item: RollingTariffBatch): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTariffBatchCodeLengthRule(var maxLength: Int) : RollingTariffBatchRule {
    override fun check(item: RollingTariffBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTariffBatchAllowedTitleRule(var allowed: MutableList<String>) : RollingTariffBatchRule {
    override fun check(item: RollingTariffBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tariff batch. */
data class RollingTariffBatchValidator(var rules: MutableList<RollingTariffBatchRule>) {
    fun validate(item: RollingTariffBatch): MutableList<String> {
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

    fun requireValid(item: RollingTariffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTariffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTariffBatchValidator(): RollingTariffBatchValidator {
    return RollingTariffBatchValidator(
        mutableListOf<RollingTariffBatchRule>(
            RollingTariffBatchRequiredNotesRule("rolling tariff batch needs this field"),
            RollingTariffBatchCodeLengthRule(255),
            RollingTariffBatchAllowedTitleRule(mutableListOf<String>("primary", "seasonal")),
        ),
    )
}
