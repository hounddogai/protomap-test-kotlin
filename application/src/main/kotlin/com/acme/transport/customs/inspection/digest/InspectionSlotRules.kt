package com.acme.transport.customs.inspection.digest

/** A rule a inspection slot must pass. Check returns its problems, or none. */
interface InspectionSlotRule {
    fun check(item: InspectionSlot): MutableList<String>
}

data class InspectionSlotCreatedAtRangeRule(var min: Int, var max: Int) : InspectionSlotRule {
    override fun check(item: InspectionSlot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSlotCodePrefixRule(var prefix: String) : InspectionSlotRule {
    override fun check(item: InspectionSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSlotRequiredNameRule(var message: String) : InspectionSlotRule {
    override fun check(item: InspectionSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionSlotRequiredCodeRule(var message: String) : InspectionSlotRule {
    override fun check(item: InspectionSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection slot. */
data class InspectionSlotValidator(var rules: MutableList<InspectionSlotRule>) {
    fun validate(item: InspectionSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionSlotValidator(): InspectionSlotValidator {
    return InspectionSlotValidator(
        mutableListOf<InspectionSlotRule>(
            InspectionSlotCreatedAtRangeRule(64, 255),
            InspectionSlotCodePrefixRule("Q"),
            InspectionSlotRequiredNameRule("inspection slot needs a value"),
            InspectionSlotRequiredCodeRule("inspection slot needs this field"),
        ),
    )
}
