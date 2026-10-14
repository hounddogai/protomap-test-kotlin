package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize

/** A rule a audit slot must pass. Check returns its problems, or none. */
interface AuditSlotRule {
    fun check(item: AuditSlot): MutableList<String>
}

data class AuditSlotLabelLengthRule(var maxLength: Int) : AuditSlotRule {
    override fun check(item: AuditSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSlotCodeLengthRule(var maxLength: Int) : AuditSlotRule {
    override fun check(item: AuditSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSlotQuantityRangeRule(var min: Int, var max: Int) : AuditSlotRule {
    override fun check(item: AuditSlot): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditSlotTimezoneLengthRule(var maxLength: Int) : AuditSlotRule {
    override fun check(item: AuditSlot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSlotAllowedTitleRule(var allowed: MutableList<String>) : AuditSlotRule {
    override fun check(item: AuditSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit slot. */
data class AuditSlotValidator(var rules: MutableList<AuditSlotRule>) {
    fun validate(item: AuditSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditSlotValidator(): AuditSlotValidator {
    return AuditSlotValidator(
        mutableListOf<AuditSlotRule>(
            AuditSlotLabelLengthRule(120),
            AuditSlotCodeLengthRule(1000),
            AuditSlotQuantityRangeRule(32, 1000),
            AuditSlotTimezoneLengthRule(3600),
            AuditSlotAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
