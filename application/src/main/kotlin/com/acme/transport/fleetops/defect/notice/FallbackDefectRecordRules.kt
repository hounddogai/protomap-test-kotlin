package com.acme.transport.fleetops.defect.notice

/** A rule a fallback defect record must pass. Check returns its problems, or none. */
interface FallbackDefectRecordRule {
    fun check(item: FallbackDefectRecord): MutableList<String>
}

data class FallbackDefectRecordRequiredLabelRule(var message: String) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectRecordRequiredUnitRule(var message: String) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectRecordRequiredCodeRule(var message: String) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectRecordCodePrefixRule(var prefix: String) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectRecordUnitLengthRule(var maxLength: Int) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectRecordRequiredCarrierRule(var message: String) : FallbackDefectRecordRule {
    override fun check(item: FallbackDefectRecord): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback defect record. */
data class FallbackDefectRecordValidator(var rules: MutableList<FallbackDefectRecordRule>) {
    fun validate(item: FallbackDefectRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDefectRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDefectRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDefectRecordValidator(): FallbackDefectRecordValidator {
    return FallbackDefectRecordValidator(
        mutableListOf<FallbackDefectRecordRule>(
            FallbackDefectRecordRequiredLabelRule("fallback defect record needs an entry"),
            FallbackDefectRecordRequiredUnitRule("fallback defect record needs a value"),
            FallbackDefectRecordRequiredCodeRule("fallback defect record needs a value"),
            FallbackDefectRecordCodePrefixRule("X"),
            FallbackDefectRecordUnitLengthRule(16),
            FallbackDefectRecordRequiredCarrierRule("fallback defect record needs this field"),
        ),
    )
}
