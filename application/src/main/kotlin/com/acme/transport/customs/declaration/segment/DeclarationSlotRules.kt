package com.acme.transport.customs.declaration.segment

/** A rule a declaration slot must pass. Check returns its problems, or none. */
interface DeclarationSlotRule {
    fun check(item: DeclarationSlot): MutableList<String>
}

data class DeclarationSlotRatioRangeRule(var min: Double, var max: Double) : DeclarationSlotRule {
    override fun check(item: DeclarationSlot): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSlotTitleLengthRule(var maxLength: Int) : DeclarationSlotRule {
    override fun check(item: DeclarationSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSlotNameLengthRule(var maxLength: Int) : DeclarationSlotRule {
    override fun check(item: DeclarationSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSlotRequiredRegionRule(var message: String) : DeclarationSlotRule {
    override fun check(item: DeclarationSlot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration slot. */
data class DeclarationSlotValidator(var rules: MutableList<DeclarationSlotRule>) {
    fun validate(item: DeclarationSlot): MutableList<String> {
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

    fun requireValid(item: DeclarationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationSlotValidator(): DeclarationSlotValidator {
    return DeclarationSlotValidator(
        mutableListOf<DeclarationSlotRule>(
            DeclarationSlotRatioRangeRule(7.5, 3459.2),
            DeclarationSlotTitleLengthRule(3600),
            DeclarationSlotNameLengthRule(255),
            DeclarationSlotRequiredRegionRule("declaration slot needs this field"),
        ),
    )
}
