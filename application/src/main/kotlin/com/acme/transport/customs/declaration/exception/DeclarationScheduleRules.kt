package com.acme.transport.customs.declaration.exception

/** A rule a declaration schedule must pass. Check returns its problems, or none. */
interface DeclarationScheduleRule {
    fun check(item: DeclarationSchedule): MutableList<String>
}

data class DeclarationScheduleNameLengthRule(var maxLength: Int) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScheduleRequiredSegmentRule(var message: String) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScheduleCodePrefixRule(var prefix: String) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScheduleContactEmailLengthRule(var maxLength: Int) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScheduleMinimumRangeRule(var min: Double, var max: Double) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationScheduleRequiredContactEmailRule(var message: String) : DeclarationScheduleRule {
    override fun check(item: DeclarationSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration schedule. */
data class DeclarationScheduleValidator(var rules: MutableList<DeclarationScheduleRule>) {
    fun validate(item: DeclarationSchedule): MutableList<String> {
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

    fun requireValid(item: DeclarationSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationScheduleValidator(): DeclarationScheduleValidator {
    return DeclarationScheduleValidator(
        mutableListOf<DeclarationScheduleRule>(
            DeclarationScheduleNameLengthRule(32),
            DeclarationScheduleRequiredSegmentRule("declaration schedule needs this field"),
            DeclarationScheduleCodePrefixRule("ACM"),
            DeclarationScheduleContactEmailLengthRule(120),
            DeclarationScheduleMinimumRangeRule(3.3, 4648.1),
            DeclarationScheduleRequiredContactEmailRule("declaration schedule needs an entry"),
        ),
    )
}
