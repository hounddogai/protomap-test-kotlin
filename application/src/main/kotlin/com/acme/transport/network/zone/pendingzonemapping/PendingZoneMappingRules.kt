package com.acme.transport.network.zone.pendingzonemapping

/** A rule a pending zone mapping must pass. Check returns its problems, or none. */
interface PendingZoneMappingRule {
    fun check(item: PendingZoneMapping): MutableList<String>
}

data class PendingZoneMappingCodePrefixRule(var prefix: String) : PendingZoneMappingRule {
    override fun check(item: PendingZoneMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingZoneMappingLabelLengthRule(var maxLength: Int) : PendingZoneMappingRule {
    override fun check(item: PendingZoneMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingZoneMappingLoadKgRangeRule(var min: Double, var max: Double) : PendingZoneMappingRule {
    override fun check(item: PendingZoneMapping): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending zone mapping. */
data class PendingZoneMappingValidator(var rules: MutableList<PendingZoneMappingRule>) {
    fun validate(item: PendingZoneMapping): MutableList<String> {
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

    fun requireValid(item: PendingZoneMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingZoneMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingZoneMappingValidator(): PendingZoneMappingValidator {
    return PendingZoneMappingValidator(
        mutableListOf<PendingZoneMappingRule>(
            PendingZoneMappingCodePrefixRule("OPS"),
            PendingZoneMappingLabelLengthRule(86400),
            PendingZoneMappingLoadKgRangeRule(8.5, 927.2),
        ),
    )
}
