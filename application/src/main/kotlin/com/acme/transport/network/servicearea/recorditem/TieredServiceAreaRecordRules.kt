package com.acme.transport.network.servicearea.recorditem

/** A rule a tiered service area record must pass. Check returns its problems, or none. */
interface TieredServiceAreaRecordRule {
    fun check(item: TieredServiceAreaRecord): MutableList<String>
}

data class TieredServiceAreaRecordNameLengthRule(var maxLength: Int) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaRecordCodeLengthRule(var maxLength: Int) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaRecordUnitLengthRule(var maxLength: Int) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaRecordRequiredCodeRule(var message: String) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaRecordRequiredUnitRule(var message: String) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaRecordTitleLengthRule(var maxLength: Int) : TieredServiceAreaRecordRule {
    override fun check(item: TieredServiceAreaRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service area record. */
data class TieredServiceAreaRecordValidator(var rules: MutableList<TieredServiceAreaRecordRule>) {
    fun validate(item: TieredServiceAreaRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredServiceAreaRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServiceAreaRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServiceAreaRecordValidator(): TieredServiceAreaRecordValidator {
    return TieredServiceAreaRecordValidator(
        mutableListOf<TieredServiceAreaRecordRule>(
            TieredServiceAreaRecordNameLengthRule(16),
            TieredServiceAreaRecordCodeLengthRule(120),
            TieredServiceAreaRecordUnitLengthRule(16),
            TieredServiceAreaRecordRequiredCodeRule("tiered service area record needs this field"),
            TieredServiceAreaRecordRequiredUnitRule("tiered service area record needs an entry"),
            TieredServiceAreaRecordTitleLengthRule(80),
        ),
    )
}
