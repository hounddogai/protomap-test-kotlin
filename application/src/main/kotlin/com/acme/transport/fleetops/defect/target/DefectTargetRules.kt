package com.acme.transport.fleetops.defect.target

/** A rule a defect target must pass. Check returns its problems, or none. */
interface DefectTargetRule {
    fun check(item: DefectTarget): MutableList<String>
}

data class DefectTargetLocaleLengthRule(var maxLength: Int) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectTargetRequiredRouteCodeRule(var message: String) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectTargetRequiredCodeRule(var message: String) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectTargetCeilingRangeRule(var min: Double, var max: Double) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class DefectTargetRevisionRangeRule(var min: Int, var max: Int) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectTargetCodePrefixRule(var prefix: String) : DefectTargetRule {
    override fun check(item: DefectTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect target. */
data class DefectTargetValidator(var rules: MutableList<DefectTargetRule>) {
    fun validate(item: DefectTarget): MutableList<String> {
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

    fun requireValid(item: DefectTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectTargetValidator(): DefectTargetValidator {
    return DefectTargetValidator(
        mutableListOf<DefectTargetRule>(
            DefectTargetLocaleLengthRule(86400),
            DefectTargetRequiredRouteCodeRule("defect target needs an entry"),
            DefectTargetRequiredCodeRule("defect target needs an entry"),
            DefectTargetCeilingRangeRule(0.3, 4315.1),
            DefectTargetRevisionRangeRule(16, 64),
            DefectTargetCodePrefixRule("X"),
        ),
    )
}
