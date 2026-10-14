package com.acme.transport.lastmile.drop.droprequest

/** A rule a drop mapping must pass. Check returns its problems, or none. */
interface DropMappingRule {
    fun check(item: DropMapping): MutableList<String>
}

data class DropMappingRequiredDepotRule(var message: String) : DropMappingRule {
    override fun check(item: DropMapping): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropMappingTrailerIdLengthRule(var maxLength: Int) : DropMappingRule {
    override fun check(item: DropMapping): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropMappingPalletsRangeRule(var min: Int, var max: Int) : DropMappingRule {
    override fun check(item: DropMapping): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropMappingCodeLengthRule(var maxLength: Int) : DropMappingRule {
    override fun check(item: DropMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop mapping. */
data class DropMappingValidator(var rules: MutableList<DropMappingRule>) {
    fun validate(item: DropMapping): MutableList<String> {
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

    fun requireValid(item: DropMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDropMappingValidator(): DropMappingValidator {
    return DropMappingValidator(
        mutableListOf<DropMappingRule>(
            DropMappingRequiredDepotRule("drop mapping needs a value"),
            DropMappingTrailerIdLengthRule(120),
            DropMappingPalletsRangeRule(120, 86400),
            DropMappingCodeLengthRule(16),
        ),
    )
}
