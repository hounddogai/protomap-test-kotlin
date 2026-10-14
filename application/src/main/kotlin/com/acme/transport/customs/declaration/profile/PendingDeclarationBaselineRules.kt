package com.acme.transport.customs.declaration.profile

/** A rule a pending declaration baseline must pass. Check returns its problems, or none. */
interface PendingDeclarationBaselineRule {
    fun check(item: PendingDeclarationBaseline): MutableList<String>
}

data class PendingDeclarationBaselineUnitLengthRule(var maxLength: Int) : PendingDeclarationBaselineRule {
    override fun check(item: PendingDeclarationBaseline): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationBaselineBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : PendingDeclarationBaselineRule {
    override fun check(item: PendingDeclarationBaseline): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationBaselineRequiredNameRule(var message: String) : PendingDeclarationBaselineRule {
    override fun check(item: PendingDeclarationBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationBaselineCodeLengthRule(var maxLength: Int) : PendingDeclarationBaselineRule {
    override fun check(item: PendingDeclarationBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending declaration baseline. */
data class PendingDeclarationBaselineValidator(var rules: MutableList<PendingDeclarationBaselineRule>) {
    fun validate(item: PendingDeclarationBaseline): MutableList<String> {
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

    fun requireValid(item: PendingDeclarationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDeclarationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDeclarationBaselineValidator(): PendingDeclarationBaselineValidator {
    return PendingDeclarationBaselineValidator(
        mutableListOf<PendingDeclarationBaselineRule>(
            PendingDeclarationBaselineUnitLengthRule(16),
            PendingDeclarationBaselineBatchSizeRangeRule(16, 120),
            PendingDeclarationBaselineRequiredNameRule("pending declaration baseline needs an entry"),
            PendingDeclarationBaselineCodeLengthRule(64),
        ),
    )
}
