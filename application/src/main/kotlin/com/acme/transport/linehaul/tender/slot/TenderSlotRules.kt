package com.acme.transport.linehaul.tender.slot

/** A rule a tender slot must pass. Check returns its problems, or none. */
interface TenderSlotRule {
    fun check(item: TenderSlot): MutableList<String>
}

data class TenderSlotCodePrefixRule(var prefix: String) : TenderSlotRule {
    override fun check(item: TenderSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderSlotTitleLengthRule(var maxLength: Int) : TenderSlotRule {
    override fun check(item: TenderSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderSlotCodeLengthRule(var maxLength: Int) : TenderSlotRule {
    override fun check(item: TenderSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender slot. */
data class TenderSlotValidator(var rules: MutableList<TenderSlotRule>) {
    fun validate(item: TenderSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderSlotValidator(): TenderSlotValidator {
    return TenderSlotValidator(
        mutableListOf<TenderSlotRule>(
            TenderSlotCodePrefixRule("ACM"),
            TenderSlotTitleLengthRule(80),
            TenderSlotCodeLengthRule(64),
        ),
    )
}
