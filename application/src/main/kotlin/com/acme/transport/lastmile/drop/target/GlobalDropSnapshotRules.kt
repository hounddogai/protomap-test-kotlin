package com.acme.transport.lastmile.drop.target

/** A rule a global drop snapshot must pass. Check returns its problems, or none. */
interface GlobalDropSnapshotRule {
    fun check(item: GlobalDropSnapshot): MutableList<String>
}

data class GlobalDropSnapshotRequiredUnitRule(var message: String) : GlobalDropSnapshotRule {
    override fun check(item: GlobalDropSnapshot): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropSnapshotRequiredCountryRule(var message: String) : GlobalDropSnapshotRule {
    override fun check(item: GlobalDropSnapshot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropSnapshotRequiredCodeRule(var message: String) : GlobalDropSnapshotRule {
    override fun check(item: GlobalDropSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global drop snapshot. */
data class GlobalDropSnapshotValidator(var rules: MutableList<GlobalDropSnapshotRule>) {
    fun validate(item: GlobalDropSnapshot): MutableList<String> {
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

    fun requireValid(item: GlobalDropSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDropSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDropSnapshotValidator(): GlobalDropSnapshotValidator {
    return GlobalDropSnapshotValidator(
        mutableListOf<GlobalDropSnapshotRule>(
            GlobalDropSnapshotRequiredUnitRule("global drop snapshot needs an entry"),
            GlobalDropSnapshotRequiredCountryRule("global drop snapshot needs a value"),
            GlobalDropSnapshotRequiredCodeRule("global drop snapshot needs a value"),
        ),
    )
}
