package com.acme.transport.fleetops.defect.target

/** A rule a defect rollup must pass. Check returns its problems, or none. */
interface DefectRollupRule {
    fun check(item: DefectRollup): MutableList<String>
}

data class DefectRollupRequiredCodeRule(var message: String) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectRollupRequiredUnitRule(var message: String) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectRollupStopCountRangeRule(var min: Int, var max: Int) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectRollupCodeLengthRule(var maxLength: Int) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRollupCodePrefixRule(var prefix: String) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectRollupRequiredNameRule(var message: String) : DefectRollupRule {
    override fun check(item: DefectRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect rollup. */
data class DefectRollupValidator(var rules: MutableList<DefectRollupRule>) {
    fun validate(item: DefectRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectRollupValidator(): DefectRollupValidator {
    return DefectRollupValidator(
        mutableListOf<DefectRollupRule>(
            DefectRollupRequiredCodeRule("defect rollup needs an entry"),
            DefectRollupRequiredUnitRule("defect rollup needs a value"),
            DefectRollupStopCountRangeRule(1000, 3600),
            DefectRollupCodeLengthRule(16),
            DefectRollupCodePrefixRule("REF"),
            DefectRollupRequiredNameRule("defect rollup needs a value"),
        ),
    )
}
