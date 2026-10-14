package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize

/** A rule a backhaul slot must pass. Check returns its problems, or none. */
interface BackhaulSlotRule {
    fun check(item: BackhaulSlot): MutableList<String>
}

data class BackhaulSlotCodeLengthRule(var maxLength: Int) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSlotAllowedNameRule(var allowed: MutableList<String>) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSlotCodePrefixRule(var prefix: String) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSlotAmountRangeRule(var min: Double, var max: Double) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSlotRequiredNameRule(var message: String) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSlotNameLengthRule(var maxLength: Int) : BackhaulSlotRule {
    override fun check(item: BackhaulSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul slot. */
data class BackhaulSlotValidator(var rules: MutableList<BackhaulSlotRule>) {
    fun validate(item: BackhaulSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulSlotValidator(): BackhaulSlotValidator {
    return BackhaulSlotValidator(
        mutableListOf<BackhaulSlotRule>(
            BackhaulSlotCodeLengthRule(16),
            BackhaulSlotAllowedNameRule(mutableListOf<String>("lenient", "capped", "pending")),
            BackhaulSlotCodePrefixRule("REF"),
            BackhaulSlotAmountRangeRule(4.2, 1302.2),
            BackhaulSlotRequiredNameRule("backhaul slot needs an entry"),
            BackhaulSlotNameLengthRule(64),
        ),
    )
}
