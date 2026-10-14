package com.acme.transport.network.cutoff.variance

/** A rule a cutoff rollup must pass. Check returns its problems, or none. */
interface CutoffRollupRule {
    fun check(item: CutoffRollup): MutableList<String>
}

data class CutoffRollupRequiredCodeRule(var message: String) : CutoffRollupRule {
    override fun check(item: CutoffRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffRollupPalletsRangeRule(var min: Int, var max: Int) : CutoffRollupRule {
    override fun check(item: CutoffRollup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffRollupNameLengthRule(var maxLength: Int) : CutoffRollupRule {
    override fun check(item: CutoffRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffRollupCodePrefixRule(var prefix: String) : CutoffRollupRule {
    override fun check(item: CutoffRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffRollupRequiredDriverIdRule(var message: String) : CutoffRollupRule {
    override fun check(item: CutoffRollup): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff rollup. */
data class CutoffRollupValidator(var rules: MutableList<CutoffRollupRule>) {
    fun validate(item: CutoffRollup): MutableList<String> {
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

    fun requireValid(item: CutoffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffRollupValidator(): CutoffRollupValidator {
    return CutoffRollupValidator(
        mutableListOf<CutoffRollupRule>(
            CutoffRollupRequiredCodeRule("cutoff rollup needs a value"),
            CutoffRollupPalletsRangeRule(80, 255),
            CutoffRollupNameLengthRule(64),
            CutoffRollupCodePrefixRule("ACM"),
            CutoffRollupRequiredDriverIdRule("cutoff rollup needs this field"),
        ),
    )
}
