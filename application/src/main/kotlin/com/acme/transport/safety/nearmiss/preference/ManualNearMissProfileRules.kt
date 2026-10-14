package com.acme.transport.safety.nearmiss.preference

/** A rule a manual near miss profile must pass. Check returns its problems, or none. */
interface ManualNearMissProfileRule {
    fun check(item: ManualNearMissProfile): MutableList<String>
}

data class ManualNearMissProfileRequiredCodeRule(var message: String) : ManualNearMissProfileRule {
    override fun check(item: ManualNearMissProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissProfileCodeLengthRule(var maxLength: Int) : ManualNearMissProfileRule {
    override fun check(item: ManualNearMissProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissProfileNameLengthRule(var maxLength: Int) : ManualNearMissProfileRule {
    override fun check(item: ManualNearMissProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualNearMissProfileRequiredExternalRefRule(var message: String) : ManualNearMissProfileRule {
    override fun check(item: ManualNearMissProfile): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual near miss profile. */
data class ManualNearMissProfileValidator(var rules: MutableList<ManualNearMissProfileRule>) {
    fun validate(item: ManualNearMissProfile): MutableList<String> {
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

    fun requireValid(item: ManualNearMissProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualNearMissProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualNearMissProfileValidator(): ManualNearMissProfileValidator {
    return ManualNearMissProfileValidator(
        mutableListOf<ManualNearMissProfileRule>(
            ManualNearMissProfileRequiredCodeRule("manual near miss profile needs an entry"),
            ManualNearMissProfileCodeLengthRule(32),
            ManualNearMissProfileNameLengthRule(32),
            ManualNearMissProfileRequiredExternalRefRule("manual near miss profile needs a value"),
        ),
    )
}
