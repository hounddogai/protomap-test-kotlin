package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize

/** A rule a tiered cross dock mapping must pass. Check returns its problems, or none. */
interface TieredCrossDockMappingRule {
    fun check(item: TieredCrossDockMapping): MutableList<String>
}

data class TieredCrossDockMappingAttemptsRangeRule(var min: Int, var max: Int) : TieredCrossDockMappingRule {
    override fun check(item: TieredCrossDockMapping): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockMappingNameLengthRule(var maxLength: Int) : TieredCrossDockMappingRule {
    override fun check(item: TieredCrossDockMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockMappingCodeLengthRule(var maxLength: Int) : TieredCrossDockMappingRule {
    override fun check(item: TieredCrossDockMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockMappingAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredCrossDockMappingRule {
    override fun check(item: TieredCrossDockMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockMappingRequiredCodeRule(var message: String) : TieredCrossDockMappingRule {
    override fun check(item: TieredCrossDockMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock mapping. */
data class TieredCrossDockMappingValidator(var rules: MutableList<TieredCrossDockMappingRule>) {
    fun validate(item: TieredCrossDockMapping): MutableList<String> {
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

    fun requireValid(item: TieredCrossDockMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockMappingValidator(): TieredCrossDockMappingValidator {
    return TieredCrossDockMappingValidator(
        mutableListOf<TieredCrossDockMappingRule>(
            TieredCrossDockMappingAttemptsRangeRule(16, 3600),
            TieredCrossDockMappingNameLengthRule(64),
            TieredCrossDockMappingCodeLengthRule(255),
            TieredCrossDockMappingAllowedNameRule(mutableListOf<String>("strict", "legacy", "lenient")),
            TieredCrossDockMappingRequiredCodeRule("tiered cross dock mapping needs a value"),
        ),
    )
}
