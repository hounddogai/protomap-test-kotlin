package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize

/** A rule a tiered declaration assignment must pass. Check returns its problems, or none. */
interface TieredDeclarationAssignmentRule {
    fun check(item: TieredDeclarationAssignment): MutableList<String>
}

data class TieredDeclarationAssignmentLimitRangeRule(
    var min: Int,
    var max: Int,
) : TieredDeclarationAssignmentRule {
    override fun check(item: TieredDeclarationAssignment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class TieredDeclarationAssignmentLabelLengthRule(var maxLength: Int) : TieredDeclarationAssignmentRule {
    override fun check(item: TieredDeclarationAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationAssignmentRequiredCodeRule(var message: String) : TieredDeclarationAssignmentRule {
    override fun check(item: TieredDeclarationAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationAssignmentRequiredLabelRule(var message: String) : TieredDeclarationAssignmentRule {
    override fun check(item: TieredDeclarationAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationAssignmentAllowedDepotRule(
    var allowed: MutableList<String>,
) : TieredDeclarationAssignmentRule {
    override fun check(item: TieredDeclarationAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration assignment. */
data class TieredDeclarationAssignmentValidator(var rules: MutableList<TieredDeclarationAssignmentRule>) {
    fun validate(item: TieredDeclarationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDeclarationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationAssignmentValidator(): TieredDeclarationAssignmentValidator {
    return TieredDeclarationAssignmentValidator(
        mutableListOf<TieredDeclarationAssignmentRule>(
            TieredDeclarationAssignmentLimitRangeRule(255, 3600),
            TieredDeclarationAssignmentLabelLengthRule(120),
            TieredDeclarationAssignmentRequiredCodeRule("tiered declaration assignment needs a value"),
            TieredDeclarationAssignmentRequiredLabelRule("tiered declaration assignment needs this field"),
            TieredDeclarationAssignmentAllowedDepotRule(mutableListOf<String>()),
        ),
    )
}
