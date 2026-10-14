package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize

/** A rule a tariff cycle must pass. Check returns its problems, or none. */
interface TariffCycleRule {
    fun check(item: TariffCycle): MutableList<String>
}

data class TariffCycleRequiredDescriptionRule(var message: String) : TariffCycleRule {
    override fun check(item: TariffCycle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffCycleLabelLengthRule(var maxLength: Int) : TariffCycleRule {
    override fun check(item: TariffCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffCycleAllowedDepotRule(var allowed: MutableList<String>) : TariffCycleRule {
    override fun check(item: TariffCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff cycle. */
data class TariffCycleValidator(var rules: MutableList<TariffCycleRule>) {
    fun validate(item: TariffCycle): MutableList<String> {
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

    fun requireValid(item: TariffCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffCycleValidator(): TariffCycleValidator {
    return TariffCycleValidator(
        mutableListOf<TariffCycleRule>(
            TariffCycleRequiredDescriptionRule("tariff cycle needs this field"),
            TariffCycleLabelLengthRule(1000),
            TariffCycleAllowedDepotRule(mutableListOf<String>("tiered", "tiered")),
        ),
    )
}
