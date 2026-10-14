package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a capped broker mapping must pass. Check returns its problems, or none. */
interface CappedBrokerMappingRule {
    fun check(item: CappedBrokerMapping): MutableList<String>
}

data class CappedBrokerMappingWeightRangeRule(var min: Double, var max: Double) : CappedBrokerMappingRule {
    override fun check(item: CappedBrokerMapping): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerMappingRequiredOwnerIdRule(var message: String) : CappedBrokerMappingRule {
    override fun check(item: CappedBrokerMapping): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerMappingDriverIdLengthRule(var maxLength: Int) : CappedBrokerMappingRule {
    override fun check(item: CappedBrokerMapping): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerMappingApprovedGuardRule(var limit: Double) : CappedBrokerMappingRule {
    override fun check(item: CappedBrokerMapping): MutableList<String> {
        if (item.approved && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerMappingAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : CappedBrokerMappingRule {
    override fun check(item: CappedBrokerMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker mapping. */
data class CappedBrokerMappingValidator(var rules: MutableList<CappedBrokerMappingRule>) {
    fun validate(item: CappedBrokerMapping): MutableList<String> {
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

    fun requireValid(item: CappedBrokerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerMappingValidator(): CappedBrokerMappingValidator {
    return CappedBrokerMappingValidator(
        mutableListOf<CappedBrokerMappingRule>(
            CappedBrokerMappingWeightRangeRule(7.3, 606.0),
            CappedBrokerMappingRequiredOwnerIdRule("capped broker mapping needs this field"),
            CappedBrokerMappingDriverIdLengthRule(80),
            CappedBrokerMappingApprovedGuardRule(5.6),
            CappedBrokerMappingAllowedContactEmailRule(mutableListOf<String>("regional")),
        ),
    )
}
