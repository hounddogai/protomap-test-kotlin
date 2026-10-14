package com.acme.transport.emissions.fuelburn.recorditem

/** A rule a primary fuel burn notice must pass. Check returns its problems, or none. */
interface PrimaryFuelBurnNoticeRule {
    fun check(item: PrimaryFuelBurnNotice): MutableList<String>
}

data class PrimaryFuelBurnNoticeCodePrefixRule(var prefix: String) : PrimaryFuelBurnNoticeRule {
    override fun check(item: PrimaryFuelBurnNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnNoticeRequiredNameRule(var message: String) : PrimaryFuelBurnNoticeRule {
    override fun check(item: PrimaryFuelBurnNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnNoticeRequiredCodeRule(var message: String) : PrimaryFuelBurnNoticeRule {
    override fun check(item: PrimaryFuelBurnNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel burn notice. */
data class PrimaryFuelBurnNoticeValidator(var rules: MutableList<PrimaryFuelBurnNoticeRule>) {
    fun validate(item: PrimaryFuelBurnNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryFuelBurnNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelBurnNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelBurnNoticeValidator(): PrimaryFuelBurnNoticeValidator {
    return PrimaryFuelBurnNoticeValidator(
        mutableListOf<PrimaryFuelBurnNoticeRule>(
            PrimaryFuelBurnNoticeCodePrefixRule("REF"),
            PrimaryFuelBurnNoticeRequiredNameRule("primary fuel burn notice needs an entry"),
            PrimaryFuelBurnNoticeRequiredCodeRule("primary fuel burn notice needs an entry"),
        ),
    )
}
