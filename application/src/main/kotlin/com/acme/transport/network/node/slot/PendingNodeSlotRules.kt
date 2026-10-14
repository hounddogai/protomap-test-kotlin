package com.acme.transport.network.node.slot

/** A rule a pending node slot must pass. Check returns its problems, or none. */
interface PendingNodeSlotRule {
    fun check(item: PendingNodeSlot): MutableList<String>
}

data class PendingNodeSlotEtaMinutesRangeRule(var min: Int, var max: Int) : PendingNodeSlotRule {
    override fun check(item: PendingNodeSlot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeSlotCodePrefixRule(var prefix: String) : PendingNodeSlotRule {
    override fun check(item: PendingNodeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeSlotRequiredCodeRule(var message: String) : PendingNodeSlotRule {
    override fun check(item: PendingNodeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeSlotOdometerRangeRule(var min: Int, var max: Int) : PendingNodeSlotRule {
    override fun check(item: PendingNodeSlot): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending node slot. */
data class PendingNodeSlotValidator(var rules: MutableList<PendingNodeSlotRule>) {
    fun validate(item: PendingNodeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNodeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodeSlotValidator(): PendingNodeSlotValidator {
    return PendingNodeSlotValidator(
        mutableListOf<PendingNodeSlotRule>(
            PendingNodeSlotEtaMinutesRangeRule(120, 86400),
            PendingNodeSlotCodePrefixRule("OPS"),
            PendingNodeSlotRequiredCodeRule("pending node slot needs an entry"),
            PendingNodeSlotOdometerRangeRule(80, 86400),
        ),
    )
}
