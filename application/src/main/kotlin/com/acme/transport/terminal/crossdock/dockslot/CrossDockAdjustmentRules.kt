package com.acme.transport.terminal.crossdock.dockslot

/** A rule a cross dock adjustment must pass. Check returns its problems, or none. */
interface CrossDockAdjustmentRule {
    fun check(item: CrossDockAdjustment): MutableList<String>
}

data class CrossDockAdjustmentNameLengthRule(var maxLength: Int) : CrossDockAdjustmentRule {
    override fun check(item: CrossDockAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAdjustmentRequiredDriverIdRule(var message: String) : CrossDockAdjustmentRule {
    override fun check(item: CrossDockAdjustment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAdjustmentDriverIdLengthRule(var maxLength: Int) : CrossDockAdjustmentRule {
    override fun check(item: CrossDockAdjustment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAdjustmentRequiredNameRule(var message: String) : CrossDockAdjustmentRule {
    override fun check(item: CrossDockAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAdjustmentCodeLengthRule(var maxLength: Int) : CrossDockAdjustmentRule {
    override fun check(item: CrossDockAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock adjustment. */
data class CrossDockAdjustmentValidator(var rules: MutableList<CrossDockAdjustmentRule>) {
    fun validate(item: CrossDockAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockAdjustmentValidator(): CrossDockAdjustmentValidator {
    return CrossDockAdjustmentValidator(
        mutableListOf<CrossDockAdjustmentRule>(
            CrossDockAdjustmentNameLengthRule(86400),
            CrossDockAdjustmentRequiredDriverIdRule("cross dock adjustment needs a value"),
            CrossDockAdjustmentDriverIdLengthRule(255),
            CrossDockAdjustmentRequiredNameRule("cross dock adjustment needs a value"),
            CrossDockAdjustmentCodeLengthRule(3600),
        ),
    )
}
