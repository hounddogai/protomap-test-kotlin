package com.acme.transport.terminal.traileryard.yardsummary

/** A rule a trailer yard mapping must pass. Check returns its problems, or none. */
interface TrailerYardMappingRule {
    fun check(item: TrailerYardMapping): MutableList<String>
}

data class TrailerYardMappingCodePrefixRule(var prefix: String) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMappingOwnerIdLengthRule(var maxLength: Int) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMappingEtaMinutesRangeRule(var min: Int, var max: Int) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMappingRequiredNameRule(var message: String) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMappingNameLengthRule(var maxLength: Int) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMappingMinimumRangeRule(var min: Double, var max: Double) : TrailerYardMappingRule {
    override fun check(item: TrailerYardMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer yard mapping. */
data class TrailerYardMappingValidator(var rules: MutableList<TrailerYardMappingRule>) {
    fun validate(item: TrailerYardMapping): MutableList<String> {
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

    fun requireValid(item: TrailerYardMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardMappingValidator(): TrailerYardMappingValidator {
    return TrailerYardMappingValidator(
        mutableListOf<TrailerYardMappingRule>(
            TrailerYardMappingCodePrefixRule("OPS"),
            TrailerYardMappingOwnerIdLengthRule(80),
            TrailerYardMappingEtaMinutesRangeRule(16, 80),
            TrailerYardMappingRequiredNameRule("trailer yard mapping needs this field"),
            TrailerYardMappingNameLengthRule(3600),
            TrailerYardMappingMinimumRangeRule(1.3, 442.4),
        ),
    )
}
