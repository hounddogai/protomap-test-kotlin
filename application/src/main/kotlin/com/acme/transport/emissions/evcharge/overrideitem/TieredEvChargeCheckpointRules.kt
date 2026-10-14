package com.acme.transport.emissions.evcharge.overrideitem

/** A rule a tiered ev charge checkpoint must pass. Check returns its problems, or none. */
interface TieredEvChargeCheckpointRule {
    fun check(item: TieredEvChargeCheckpoint): MutableList<String>
}

data class TieredEvChargeCheckpointCurrencyLengthRule(var maxLength: Int) : TieredEvChargeCheckpointRule {
    override fun check(item: TieredEvChargeCheckpoint): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeCheckpointNameLengthRule(var maxLength: Int) : TieredEvChargeCheckpointRule {
    override fun check(item: TieredEvChargeCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeCheckpointRequiredCodeRule(var message: String) : TieredEvChargeCheckpointRule {
    override fun check(item: TieredEvChargeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeCheckpointLocaleLengthRule(var maxLength: Int) : TieredEvChargeCheckpointRule {
    override fun check(item: TieredEvChargeCheckpoint): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeCheckpointScoreRangeRule(
    var min: Double,
    var max: Double,
) : TieredEvChargeCheckpointRule {
    override fun check(item: TieredEvChargeCheckpoint): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered ev charge checkpoint. */
data class TieredEvChargeCheckpointValidator(var rules: MutableList<TieredEvChargeCheckpointRule>) {
    fun validate(item: TieredEvChargeCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEvChargeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargeCheckpointValidator(): TieredEvChargeCheckpointValidator {
    return TieredEvChargeCheckpointValidator(
        mutableListOf<TieredEvChargeCheckpointRule>(
            TieredEvChargeCheckpointCurrencyLengthRule(3600),
            TieredEvChargeCheckpointNameLengthRule(120),
            TieredEvChargeCheckpointRequiredCodeRule("tiered ev charge checkpoint needs an entry"),
            TieredEvChargeCheckpointLocaleLengthRule(80),
            TieredEvChargeCheckpointScoreRangeRule(9.4, 4735.7),
        ),
    )
}
