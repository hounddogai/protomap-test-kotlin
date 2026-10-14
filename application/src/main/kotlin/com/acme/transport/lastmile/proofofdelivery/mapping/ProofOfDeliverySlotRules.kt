package com.acme.transport.lastmile.proofofdelivery.mapping

/** A rule a proof of delivery slot must pass. Check returns its problems, or none. */
interface ProofOfDeliverySlotRule {
    fun check(item: ProofOfDeliverySlot): MutableList<String>
}

data class ProofOfDeliverySlotRequiredRouteCodeRule(var message: String) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySlotCodeLengthRule(var maxLength: Int) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySlotQuantityRangeRule(var min: Int, var max: Int) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliverySlotRouteCodeLengthRule(var maxLength: Int) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySlotLabelLengthRule(var maxLength: Int) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySlotCodePrefixRule(var prefix: String) : ProofOfDeliverySlotRule {
    override fun check(item: ProofOfDeliverySlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery slot. */
data class ProofOfDeliverySlotValidator(var rules: MutableList<ProofOfDeliverySlotRule>) {
    fun validate(item: ProofOfDeliverySlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliverySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliverySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliverySlotValidator(): ProofOfDeliverySlotValidator {
    return ProofOfDeliverySlotValidator(
        mutableListOf<ProofOfDeliverySlotRule>(
            ProofOfDeliverySlotRequiredRouteCodeRule("proof of delivery slot needs an entry"),
            ProofOfDeliverySlotCodeLengthRule(80),
            ProofOfDeliverySlotQuantityRangeRule(255, 86400),
            ProofOfDeliverySlotRouteCodeLengthRule(3600),
            ProofOfDeliverySlotLabelLengthRule(80),
            ProofOfDeliverySlotCodePrefixRule("X"),
        ),
    )
}
