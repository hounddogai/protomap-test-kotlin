package com.acme.transport.customs.tariff.checkpoint

/** A rule a manual tariff checkpoint must pass. Check returns its problems, or none. */
interface ManualTariffCheckpointRule {
    fun check(item: ManualTariffCheckpoint): MutableList<String>
}

data class ManualTariffCheckpointRequiredTitleRule(var message: String) : ManualTariffCheckpointRule {
    override fun check(item: ManualTariffCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffCheckpointScoreRangeRule(var min: Double, var max: Double) : ManualTariffCheckpointRule {
    override fun check(item: ManualTariffCheckpoint): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffCheckpointNameLengthRule(var maxLength: Int) : ManualTariffCheckpointRule {
    override fun check(item: ManualTariffCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffCheckpointCodePrefixRule(var prefix: String) : ManualTariffCheckpointRule {
    override fun check(item: ManualTariffCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffCheckpointRequiredNameRule(var message: String) : ManualTariffCheckpointRule {
    override fun check(item: ManualTariffCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tariff checkpoint. */
data class ManualTariffCheckpointValidator(var rules: MutableList<ManualTariffCheckpointRule>) {
    fun validate(item: ManualTariffCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTariffCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTariffCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTariffCheckpointValidator(): ManualTariffCheckpointValidator {
    return ManualTariffCheckpointValidator(
        mutableListOf<ManualTariffCheckpointRule>(
            ManualTariffCheckpointRequiredTitleRule("manual tariff checkpoint needs an entry"),
            ManualTariffCheckpointScoreRangeRule(5.0, 4697.1),
            ManualTariffCheckpointNameLengthRule(86400),
            ManualTariffCheckpointCodePrefixRule("X"),
            ManualTariffCheckpointRequiredNameRule("manual tariff checkpoint needs this field"),
        ),
    )
}
