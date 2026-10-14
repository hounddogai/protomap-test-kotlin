package com.acme.transport.customs.tariff.variance

/** A rule a global tariff slot must pass. Check returns its problems, or none. */
interface GlobalTariffSlotRule {
    fun check(item: GlobalTariffSlot): MutableList<String>
}

data class GlobalTariffSlotRequiredDepotRule(var message: String) : GlobalTariffSlotRule {
    override fun check(item: GlobalTariffSlot): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffSlotBatchSizeRangeRule(var min: Int, var max: Int) : GlobalTariffSlotRule {
    override fun check(item: GlobalTariffSlot): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffSlotDepotLengthRule(var maxLength: Int) : GlobalTariffSlotRule {
    override fun check(item: GlobalTariffSlot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTariffSlotOdometerRangeRule(var min: Int, var max: Int) : GlobalTariffSlotRule {
    override fun check(item: GlobalTariffSlot): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tariff slot. */
data class GlobalTariffSlotValidator(var rules: MutableList<GlobalTariffSlotRule>) {
    fun validate(item: GlobalTariffSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTariffSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTariffSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTariffSlotValidator(): GlobalTariffSlotValidator {
    return GlobalTariffSlotValidator(
        mutableListOf<GlobalTariffSlotRule>(
            GlobalTariffSlotRequiredDepotRule("global tariff slot needs an entry"),
            GlobalTariffSlotBatchSizeRangeRule(1000, 3600),
            GlobalTariffSlotDepotLengthRule(64),
            GlobalTariffSlotOdometerRangeRule(16, 3600),
        ),
    )
}
