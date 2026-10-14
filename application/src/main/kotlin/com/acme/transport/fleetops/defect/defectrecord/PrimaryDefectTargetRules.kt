package com.acme.transport.fleetops.defect.defectrecord

/** A rule a primary defect target must pass. Check returns its problems, or none. */
interface PrimaryDefectTargetRule {
    fun check(item: PrimaryDefectTarget): MutableList<String>
}

data class PrimaryDefectTargetExpiresAtRangeRule(var min: Int, var max: Int) : PrimaryDefectTargetRule {
    override fun check(item: PrimaryDefectTarget): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class PrimaryDefectTargetCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryDefectTargetRule {
    override fun check(item: PrimaryDefectTarget): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class PrimaryDefectTargetCodePrefixRule(var prefix: String) : PrimaryDefectTargetRule {
    override fun check(item: PrimaryDefectTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDefectTargetRequiredShiftCodeRule(var message: String) : PrimaryDefectTargetRule {
    override fun check(item: PrimaryDefectTarget): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDefectTargetCodeLengthRule(var maxLength: Int) : PrimaryDefectTargetRule {
    override fun check(item: PrimaryDefectTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary defect target. */
data class PrimaryDefectTargetValidator(var rules: MutableList<PrimaryDefectTargetRule>) {
    fun validate(item: PrimaryDefectTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDefectTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDefectTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDefectTargetValidator(): PrimaryDefectTargetValidator {
    return PrimaryDefectTargetValidator(
        mutableListOf<PrimaryDefectTargetRule>(
            PrimaryDefectTargetExpiresAtRangeRule(255, 86400),
            PrimaryDefectTargetCreatedAtRangeRule(16, 3600),
            PrimaryDefectTargetCodePrefixRule("ACM"),
            PrimaryDefectTargetRequiredShiftCodeRule("primary defect target needs a value"),
            PrimaryDefectTargetCodeLengthRule(16),
        ),
    )
}
