package com.acme.transport.terminal.traileryard.plan

/** A rule a manual trailer yard adjustment must pass. Check returns its problems, or none. */
interface ManualTrailerYardAdjustmentRule {
    fun check(item: ManualTrailerYardAdjustment): MutableList<String>
}

data class ManualTrailerYardAdjustmentRequiredCodeRule(var message: String) : ManualTrailerYardAdjustmentRule {
    override fun check(item: ManualTrailerYardAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAdjustmentRequiredTrailerIdRule(
    var message: String,
) : ManualTrailerYardAdjustmentRule {
    override fun check(item: ManualTrailerYardAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAdjustmentTrailerIdLengthRule(
    var maxLength: Int,
) : ManualTrailerYardAdjustmentRule {
    override fun check(item: ManualTrailerYardAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAdjustmentCodeLengthRule(var maxLength: Int) : ManualTrailerYardAdjustmentRule {
    override fun check(item: ManualTrailerYardAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard adjustment. */
data class ManualTrailerYardAdjustmentValidator(var rules: MutableList<ManualTrailerYardAdjustmentRule>) {
    fun validate(item: ManualTrailerYardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerYardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardAdjustmentValidator(): ManualTrailerYardAdjustmentValidator {
    return ManualTrailerYardAdjustmentValidator(
        mutableListOf<ManualTrailerYardAdjustmentRule>(
            ManualTrailerYardAdjustmentRequiredCodeRule("manual trailer yard adjustment needs this field"),
            ManualTrailerYardAdjustmentRequiredTrailerIdRule("manual trailer yard adjustment needs a value"),
            ManualTrailerYardAdjustmentTrailerIdLengthRule(86400),
            ManualTrailerYardAdjustmentCodeLengthRule(86400),
        ),
    )
}
