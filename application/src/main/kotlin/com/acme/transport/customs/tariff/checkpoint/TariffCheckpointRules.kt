package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a tariff checkpoint must pass. Check returns its problems, or none. */
interface TariffCheckpointRule {
    fun check(item: TariffCheckpoint): MutableList<String>
}

data class TariffCheckpointLabelLengthRule(var maxLength: Int) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffCheckpointAllowedCodeRule(var allowed: MutableList<String>) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TariffCheckpointCountryLengthRule(var maxLength: Int) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffCheckpointRequiredCodeRule(var message: String) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffCheckpointRequiredCountryRule(var message: String) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffCheckpointCodeLengthRule(var maxLength: Int) : TariffCheckpointRule {
    override fun check(item: TariffCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff checkpoint. */
data class TariffCheckpointValidator(var rules: MutableList<TariffCheckpointRule>) {
    fun validate(item: TariffCheckpoint): MutableList<String> {
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

    fun requireValid(item: TariffCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffCheckpointValidator(): TariffCheckpointValidator {
    return TariffCheckpointValidator(
        mutableListOf<TariffCheckpointRule>(
            TariffCheckpointLabelLengthRule(16),
            TariffCheckpointAllowedCodeRule(mutableListOf<String>("adaptive", "scheduled", "seasonal")),
            TariffCheckpointCountryLengthRule(255),
            TariffCheckpointRequiredCodeRule("tariff checkpoint needs an entry"),
            TariffCheckpointRequiredCountryRule("tariff checkpoint needs an entry"),
            TariffCheckpointCodeLengthRule(64),
        ),
    )
}
