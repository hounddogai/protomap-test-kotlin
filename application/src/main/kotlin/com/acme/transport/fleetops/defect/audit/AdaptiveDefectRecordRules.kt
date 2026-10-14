package com.acme.transport.fleetops.defect.audit

/** A rule a adaptive defect record must pass. Check returns its problems, or none. */
interface AdaptiveDefectRecordRule {
    fun check(item: AdaptiveDefectRecord): MutableList<String>
}

data class AdaptiveDefectRecordNameLengthRule(var maxLength: Int) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectRecordActiveGuardRule(var limit: Int) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        if (item.active && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectRecordDescriptionLengthRule(var maxLength: Int) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectRecordRequiredDescriptionRule(var message: String) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectRecordDockLengthRule(var maxLength: Int) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectRecordRequiredDockRule(var message: String) : AdaptiveDefectRecordRule {
    override fun check(item: AdaptiveDefectRecord): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive defect record. */
data class AdaptiveDefectRecordValidator(var rules: MutableList<AdaptiveDefectRecordRule>) {
    fun validate(item: AdaptiveDefectRecord): MutableList<String> {
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

    fun requireValid(item: AdaptiveDefectRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDefectRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDefectRecordValidator(): AdaptiveDefectRecordValidator {
    return AdaptiveDefectRecordValidator(
        mutableListOf<AdaptiveDefectRecordRule>(
            AdaptiveDefectRecordNameLengthRule(32),
            AdaptiveDefectRecordActiveGuardRule(32),
            AdaptiveDefectRecordDescriptionLengthRule(80),
            AdaptiveDefectRecordRequiredDescriptionRule("adaptive defect record needs this field"),
            AdaptiveDefectRecordDockLengthRule(1000),
            AdaptiveDefectRecordRequiredDockRule("adaptive defect record needs an entry"),
        ),
    )
}
