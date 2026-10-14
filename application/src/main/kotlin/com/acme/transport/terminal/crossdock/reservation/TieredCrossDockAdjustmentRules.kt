package com.acme.transport.terminal.crossdock.reservation

/** A rule a tiered cross dock adjustment must pass. Check returns its problems, or none. */
interface TieredCrossDockAdjustmentRule {
    fun check(item: TieredCrossDockAdjustment): MutableList<String>
}

data class TieredCrossDockAdjustmentRequiredNameRule(var message: String) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockAdjustmentActiveGuardRule(var limit: Double) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockAdjustmentRequiredNotesRule(var message: String) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockAdjustmentNameLengthRule(var maxLength: Int) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockAdjustmentRequiredCodeRule(var message: String) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockAdjustmentNotesLengthRule(var maxLength: Int) : TieredCrossDockAdjustmentRule {
    override fun check(item: TieredCrossDockAdjustment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock adjustment. */
data class TieredCrossDockAdjustmentValidator(var rules: MutableList<TieredCrossDockAdjustmentRule>) {
    fun validate(item: TieredCrossDockAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCrossDockAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockAdjustmentValidator(): TieredCrossDockAdjustmentValidator {
    return TieredCrossDockAdjustmentValidator(
        mutableListOf<TieredCrossDockAdjustmentRule>(
            TieredCrossDockAdjustmentRequiredNameRule("tiered cross dock adjustment needs an entry"),
            TieredCrossDockAdjustmentActiveGuardRule(4.2),
            TieredCrossDockAdjustmentRequiredNotesRule("tiered cross dock adjustment needs an entry"),
            TieredCrossDockAdjustmentNameLengthRule(64),
            TieredCrossDockAdjustmentRequiredCodeRule("tiered cross dock adjustment needs a value"),
            TieredCrossDockAdjustmentNotesLengthRule(3600),
        ),
    )
}
