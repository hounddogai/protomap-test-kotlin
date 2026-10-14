package com.acme.transport.terminal.dockdoor.template

/** A rule a global dock door template must pass. Check returns its problems, or none. */
interface GlobalDockDoorTemplateRule {
    fun check(item: GlobalDockDoorTemplate): MutableList<String>
}

data class GlobalDockDoorTemplateCodeLengthRule(var maxLength: Int) : GlobalDockDoorTemplateRule {
    override fun check(item: GlobalDockDoorTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorTemplateNameLengthRule(var maxLength: Int) : GlobalDockDoorTemplateRule {
    override fun check(item: GlobalDockDoorTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorTemplateStopCountRangeRule(var min: Int, var max: Int) : GlobalDockDoorTemplateRule {
    override fun check(item: GlobalDockDoorTemplate): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global dock door template. */
data class GlobalDockDoorTemplateValidator(var rules: MutableList<GlobalDockDoorTemplateRule>) {
    fun validate(item: GlobalDockDoorTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDockDoorTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDockDoorTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDockDoorTemplateValidator(): GlobalDockDoorTemplateValidator {
    return GlobalDockDoorTemplateValidator(
        mutableListOf<GlobalDockDoorTemplateRule>(
            GlobalDockDoorTemplateCodeLengthRule(64),
            GlobalDockDoorTemplateNameLengthRule(80),
            GlobalDockDoorTemplateStopCountRangeRule(80, 1000),
        ),
    )
}
