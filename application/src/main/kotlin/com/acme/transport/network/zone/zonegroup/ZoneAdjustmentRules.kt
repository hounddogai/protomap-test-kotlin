package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.text.normalize

/** A rule a zone adjustment must pass. Check returns its problems, or none. */
interface ZoneAdjustmentRule {
    fun check(item: ZoneAdjustment): MutableList<String>
}

data class ZoneAdjustmentStartsAtRangeRule(var min: Int, var max: Int) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAdjustmentRequiredTitleRule(var message: String) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAdjustmentRequiredLaneRule(var message: String) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAdjustmentRequiredShiftCodeRule(var message: String) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAdjustmentRequiredCodeRule(var message: String) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : ZoneAdjustmentRule {
    override fun check(item: ZoneAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone adjustment. */
data class ZoneAdjustmentValidator(var rules: MutableList<ZoneAdjustmentRule>) {
    fun validate(item: ZoneAdjustment): MutableList<String> {
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

    fun requireValid(item: ZoneAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneAdjustmentValidator(): ZoneAdjustmentValidator {
    return ZoneAdjustmentValidator(
        mutableListOf<ZoneAdjustmentRule>(
            ZoneAdjustmentStartsAtRangeRule(255, 3600),
            ZoneAdjustmentRequiredTitleRule("zone adjustment needs a value"),
            ZoneAdjustmentRequiredLaneRule("zone adjustment needs this field"),
            ZoneAdjustmentRequiredShiftCodeRule("zone adjustment needs an entry"),
            ZoneAdjustmentRequiredCodeRule("zone adjustment needs a value"),
            ZoneAdjustmentAllowedCodeRule(mutableListOf<String>("scheduled", "pending", "manual")),
        ),
    )
}
