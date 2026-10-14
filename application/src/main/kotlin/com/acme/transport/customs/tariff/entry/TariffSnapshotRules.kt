package com.acme.transport.customs.tariff.entry

/** A rule a tariff snapshot must pass. Check returns its problems, or none. */
interface TariffSnapshotRule {
    fun check(item: TariffSnapshot): MutableList<String>
}

data class TariffSnapshotRouteCodeLengthRule(var maxLength: Int) : TariffSnapshotRule {
    override fun check(item: TariffSnapshot): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffSnapshotRequiredNameRule(var message: String) : TariffSnapshotRule {
    override fun check(item: TariffSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffSnapshotCodeLengthRule(var maxLength: Int) : TariffSnapshotRule {
    override fun check(item: TariffSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffSnapshotRequiredRouteCodeRule(var message: String) : TariffSnapshotRule {
    override fun check(item: TariffSnapshot): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff snapshot. */
data class TariffSnapshotValidator(var rules: MutableList<TariffSnapshotRule>) {
    fun validate(item: TariffSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffSnapshotValidator(): TariffSnapshotValidator {
    return TariffSnapshotValidator(
        mutableListOf<TariffSnapshotRule>(
            TariffSnapshotRouteCodeLengthRule(255),
            TariffSnapshotRequiredNameRule("tariff snapshot needs a value"),
            TariffSnapshotCodeLengthRule(32),
            TariffSnapshotRequiredRouteCodeRule("tariff snapshot needs an entry"),
        ),
    )
}
