package com.acme.transport.lastmile.redelivery.digest

/** A rule a redelivery slot must pass. Check returns its problems, or none. */
interface RedeliverySlotRule {
    fun check(item: RedeliverySlot): MutableList<String>
}

data class RedeliverySlotLabelLengthRule(var maxLength: Int) : RedeliverySlotRule {
    override fun check(item: RedeliverySlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySlotCodeLengthRule(var maxLength: Int) : RedeliverySlotRule {
    override fun check(item: RedeliverySlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySlotRequiredLabelRule(var message: String) : RedeliverySlotRule {
    override fun check(item: RedeliverySlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySlotRequiredCodeRule(var message: String) : RedeliverySlotRule {
    override fun check(item: RedeliverySlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery slot. */
data class RedeliverySlotValidator(var rules: MutableList<RedeliverySlotRule>) {
    fun validate(item: RedeliverySlot): MutableList<String> {
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

    fun requireValid(item: RedeliverySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliverySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliverySlotValidator(): RedeliverySlotValidator {
    return RedeliverySlotValidator(
        mutableListOf<RedeliverySlotRule>(
            RedeliverySlotLabelLengthRule(16),
            RedeliverySlotCodeLengthRule(16),
            RedeliverySlotRequiredLabelRule("redelivery slot needs this field"),
            RedeliverySlotRequiredCodeRule("redelivery slot needs this field"),
        ),
    )
}
