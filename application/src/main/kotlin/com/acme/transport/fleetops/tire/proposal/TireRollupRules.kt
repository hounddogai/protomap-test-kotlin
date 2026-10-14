package com.acme.transport.fleetops.tire.proposal

/** A rule a tire rollup must pass. Check returns its problems, or none. */
interface TireRollupRule {
    fun check(item: TireRollup): MutableList<String>
}

data class TireRollupRequiredCodeRule(var message: String) : TireRollupRule {
    override fun check(item: TireRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireRollupDockLengthRule(var maxLength: Int) : TireRollupRule {
    override fun check(item: TireRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireRollupLabelLengthRule(var maxLength: Int) : TireRollupRule {
    override fun check(item: TireRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireRollupCodePrefixRule(var prefix: String) : TireRollupRule {
    override fun check(item: TireRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireRollupCodeLengthRule(var maxLength: Int) : TireRollupRule {
    override fun check(item: TireRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire rollup. */
data class TireRollupValidator(var rules: MutableList<TireRollupRule>) {
    fun validate(item: TireRollup): MutableList<String> {
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

    fun requireValid(item: TireRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTireRollupValidator(): TireRollupValidator {
    return TireRollupValidator(
        mutableListOf<TireRollupRule>(
            TireRollupRequiredCodeRule("tire rollup needs an entry"),
            TireRollupDockLengthRule(120),
            TireRollupLabelLengthRule(1000),
            TireRollupCodePrefixRule("ACM"),
            TireRollupCodeLengthRule(32),
        ),
    )
}
