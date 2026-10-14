package com.acme.transport.freightpay.audit.slot

/** A rule a rolling audit mapping must pass. Check returns its problems, or none. */
interface RollingAuditMappingRule {
    fun check(item: RollingAuditMapping): MutableList<String>
}

data class RollingAuditMappingArchivedGuardRule(var limit: Double) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditMappingThresholdRangeRule(var min: Double, var max: Double) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RollingAuditMappingRequiredNotesRule(var message: String) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditMappingNameLengthRule(var maxLength: Int) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditMappingCodeLengthRule(var maxLength: Int) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditMappingRequiredNameRule(var message: String) : RollingAuditMappingRule {
    override fun check(item: RollingAuditMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling audit mapping. */
data class RollingAuditMappingValidator(var rules: MutableList<RollingAuditMappingRule>) {
    fun validate(item: RollingAuditMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAuditMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAuditMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAuditMappingValidator(): RollingAuditMappingValidator {
    return RollingAuditMappingValidator(
        mutableListOf<RollingAuditMappingRule>(
            RollingAuditMappingArchivedGuardRule(5.6),
            RollingAuditMappingThresholdRangeRule(2.0, 2642.8),
            RollingAuditMappingRequiredNotesRule("rolling audit mapping needs an entry"),
            RollingAuditMappingNameLengthRule(120),
            RollingAuditMappingCodeLengthRule(3600),
            RollingAuditMappingRequiredNameRule("rolling audit mapping needs this field"),
        ),
    )
}
