package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard audit must pass. Check returns its problems, or none. */
interface TrailerYardAuditRule {
    fun check(item: TrailerYardAudit): MutableList<String>
}

data class TrailerYardAuditAllowedContactEmailRule(var allowed: MutableList<String>) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAuditCodePrefixRule(var prefix: String) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAuditRequiredRegionRule(var message: String) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAuditRequiredContactEmailRule(var message: String) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAuditFloorValueRangeRule(var min: Double, var max: Double) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAuditWeightRangeRule(var min: Double, var max: Double) : TrailerYardAuditRule {
    override fun check(item: TrailerYardAudit): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard audit. */
data class TrailerYardAuditValidator(var rules: MutableList<TrailerYardAuditRule>) {
    fun validate(item: TrailerYardAudit): MutableList<String> {
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

    fun requireValid(item: TrailerYardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardAuditValidator(): TrailerYardAuditValidator {
    return TrailerYardAuditValidator(
        mutableListOf<TrailerYardAuditRule>(
            TrailerYardAuditAllowedContactEmailRule(mutableListOf<String>("global")),
            TrailerYardAuditCodePrefixRule("ACM"),
            TrailerYardAuditRequiredRegionRule("trailer yard audit needs a value"),
            TrailerYardAuditRequiredContactEmailRule("trailer yard audit needs a value"),
            TrailerYardAuditFloorValueRangeRule(8.3, 272.8),
            TrailerYardAuditWeightRangeRule(6.6, 1890.1),
        ),
    )
}
