package com.acme.transport.customs.tariff.tariffdraft

/** A rule a tariff bundle must pass. Check returns its problems, or none. */
interface TariffBundleRule {
    fun check(item: TariffBundle): MutableList<String>
}

data class TariffBundleRequiredCategoryRule(var message: String) : TariffBundleRule {
    override fun check(item: TariffBundle): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffBundleRequiredNotesRule(var message: String) : TariffBundleRule {
    override fun check(item: TariffBundle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffBundleRequiredCodeRule(var message: String) : TariffBundleRule {
    override fun check(item: TariffBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffBundleNotesLengthRule(var maxLength: Int) : TariffBundleRule {
    override fun check(item: TariffBundle): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff bundle. */
data class TariffBundleValidator(var rules: MutableList<TariffBundleRule>) {
    fun validate(item: TariffBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffBundleValidator(): TariffBundleValidator {
    return TariffBundleValidator(
        mutableListOf<TariffBundleRule>(
            TariffBundleRequiredCategoryRule("tariff bundle needs this field"),
            TariffBundleRequiredNotesRule("tariff bundle needs a value"),
            TariffBundleRequiredCodeRule("tariff bundle needs this field"),
            TariffBundleNotesLengthRule(255),
        ),
    )
}
