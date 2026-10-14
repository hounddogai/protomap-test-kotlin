package com.acme.transport.customs.broker.budget

/** A rule a broker slot must pass. Check returns its problems, or none. */
interface BrokerSlotRule {
    fun check(item: BrokerSlot): MutableList<String>
}

data class BrokerSlotNameLengthRule(var maxLength: Int) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSlotCodeLengthRule(var maxLength: Int) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSlotRequiredLaneRule(var message: String) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerSlotLaneLengthRule(var maxLength: Int) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSlotSegmentLengthRule(var maxLength: Int) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSlotCodePrefixRule(var prefix: String) : BrokerSlotRule {
    override fun check(item: BrokerSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker slot. */
data class BrokerSlotValidator(var rules: MutableList<BrokerSlotRule>) {
    fun validate(item: BrokerSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerSlotValidator(): BrokerSlotValidator {
    return BrokerSlotValidator(
        mutableListOf<BrokerSlotRule>(
            BrokerSlotNameLengthRule(80),
            BrokerSlotCodeLengthRule(3600),
            BrokerSlotRequiredLaneRule("broker slot needs this field"),
            BrokerSlotLaneLengthRule(64),
            BrokerSlotSegmentLengthRule(120),
            BrokerSlotCodePrefixRule("ACM"),
        ),
    )
}
