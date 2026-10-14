package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.normalize

/** A rule a dock door tier must pass. Check returns its problems, or none. */
interface DockDoorTierRule {
    fun check(item: DockDoorTier): MutableList<String>
}

data class DockDoorTierFloorValueRangeRule(var min: Double, var max: Double) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTierRequiredNameRule(var message: String) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTierUnitLengthRule(var maxLength: Int) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTierRequiredCodeRule(var message: String) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTierCarrierLengthRule(var maxLength: Int) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTierAllowedLabelRule(var allowed: MutableList<String>) : DockDoorTierRule {
    override fun check(item: DockDoorTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door tier. */
data class DockDoorTierValidator(var rules: MutableList<DockDoorTierRule>) {
    fun validate(item: DockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorTierValidator(): DockDoorTierValidator {
    return DockDoorTierValidator(
        mutableListOf<DockDoorTierRule>(
            DockDoorTierFloorValueRangeRule(7.1, 3268.4),
            DockDoorTierRequiredNameRule("dock door tier needs this field"),
            DockDoorTierUnitLengthRule(86400),
            DockDoorTierRequiredCodeRule("dock door tier needs this field"),
            DockDoorTierCarrierLengthRule(1000),
            DockDoorTierAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
