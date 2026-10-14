package com.acme.transport.network.servicearea.areathreshold

/** A rule a default service area rollup must pass. Check returns its problems, or none. */
interface DefaultServiceAreaRollupRule {
    fun check(item: DefaultServiceAreaRollup): MutableList<String>
}

data class DefaultServiceAreaRollupRequiredCodeRule(var message: String) : DefaultServiceAreaRollupRule {
    override fun check(item: DefaultServiceAreaRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaRollupShiftCodeLengthRule(var maxLength: Int) : DefaultServiceAreaRollupRule {
    override fun check(item: DefaultServiceAreaRollup): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaRollupCodeLengthRule(var maxLength: Int) : DefaultServiceAreaRollupRule {
    override fun check(item: DefaultServiceAreaRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaRollupRequiredShiftCodeRule(var message: String) : DefaultServiceAreaRollupRule {
    override fun check(item: DefaultServiceAreaRollup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service area rollup. */
data class DefaultServiceAreaRollupValidator(var rules: MutableList<DefaultServiceAreaRollupRule>) {
    fun validate(item: DefaultServiceAreaRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServiceAreaRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServiceAreaRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServiceAreaRollupValidator(): DefaultServiceAreaRollupValidator {
    return DefaultServiceAreaRollupValidator(
        mutableListOf<DefaultServiceAreaRollupRule>(
            DefaultServiceAreaRollupRequiredCodeRule("default service area rollup needs a value"),
            DefaultServiceAreaRollupShiftCodeLengthRule(80),
            DefaultServiceAreaRollupCodeLengthRule(32),
            DefaultServiceAreaRollupRequiredShiftCodeRule("default service area rollup needs this field"),
        ),
    )
}
