package com.acme.transport.safety.cameraevent.adjustment

/** A rule a legacy camera event adjustment must pass. Check returns its problems, or none. */
interface LegacyCameraEventAdjustmentRule {
    fun check(item: LegacyCameraEventAdjustment): MutableList<String>
}

data class LegacyCameraEventAdjustmentRequiredNameRule(var message: String) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAdjustmentCodeLengthRule(var maxLength: Int) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAdjustmentNameLengthRule(var maxLength: Int) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAdjustmentRequiredCodeRule(var message: String) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAdjustmentCategoryLengthRule(var maxLength: Int) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventAdjustmentRequiredCategoryRule(
    var message: String,
) : LegacyCameraEventAdjustmentRule {
    override fun check(item: LegacyCameraEventAdjustment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy camera event adjustment. */
data class LegacyCameraEventAdjustmentValidator(var rules: MutableList<LegacyCameraEventAdjustmentRule>) {
    fun validate(item: LegacyCameraEventAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCameraEventAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCameraEventAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCameraEventAdjustmentValidator(): LegacyCameraEventAdjustmentValidator {
    return LegacyCameraEventAdjustmentValidator(
        mutableListOf<LegacyCameraEventAdjustmentRule>(
            LegacyCameraEventAdjustmentRequiredNameRule("legacy camera event adjustment needs an entry"),
            LegacyCameraEventAdjustmentCodeLengthRule(255),
            LegacyCameraEventAdjustmentNameLengthRule(255),
            LegacyCameraEventAdjustmentRequiredCodeRule("legacy camera event adjustment needs a value"),
            LegacyCameraEventAdjustmentCategoryLengthRule(32),
            LegacyCameraEventAdjustmentRequiredCategoryRule("legacy camera event adjustment needs this field"),
        ),
    )
}
