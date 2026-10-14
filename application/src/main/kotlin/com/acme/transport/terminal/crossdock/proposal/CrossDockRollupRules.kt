package com.acme.transport.terminal.crossdock.proposal

/** A rule a cross dock rollup must pass. Check returns its problems, or none. */
interface CrossDockRollupRule {
    fun check(item: CrossDockRollup): MutableList<String>
}

data class CrossDockRollupCodePrefixRule(var prefix: String) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRollupRequiredCodeRule(var message: String) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRollupRequiredDescriptionRule(var message: String) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRollupCodeLengthRule(var maxLength: Int) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRollupLabelLengthRule(var maxLength: Int) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRollupRequiredLabelRule(var message: String) : CrossDockRollupRule {
    override fun check(item: CrossDockRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock rollup. */
data class CrossDockRollupValidator(var rules: MutableList<CrossDockRollupRule>) {
    fun validate(item: CrossDockRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockRollupValidator(): CrossDockRollupValidator {
    return CrossDockRollupValidator(
        mutableListOf<CrossDockRollupRule>(
            CrossDockRollupCodePrefixRule("X"),
            CrossDockRollupRequiredCodeRule("cross dock rollup needs this field"),
            CrossDockRollupRequiredDescriptionRule("cross dock rollup needs an entry"),
            CrossDockRollupCodeLengthRule(32),
            CrossDockRollupLabelLengthRule(64),
            CrossDockRollupRequiredLabelRule("cross dock rollup needs a value"),
        ),
    )
}
