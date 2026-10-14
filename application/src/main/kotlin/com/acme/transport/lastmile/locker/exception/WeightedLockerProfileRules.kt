package com.acme.transport.lastmile.locker.exception

/** A rule a weighted locker profile must pass. Check returns its problems, or none. */
interface WeightedLockerProfileRule {
    fun check(item: WeightedLockerProfile): MutableList<String>
}

data class WeightedLockerProfileRequiredDockRule(var message: String) : WeightedLockerProfileRule {
    override fun check(item: WeightedLockerProfile): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerProfileCodeLengthRule(var maxLength: Int) : WeightedLockerProfileRule {
    override fun check(item: WeightedLockerProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerProfileRequiredTitleRule(var message: String) : WeightedLockerProfileRule {
    override fun check(item: WeightedLockerProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted locker profile. */
data class WeightedLockerProfileValidator(var rules: MutableList<WeightedLockerProfileRule>) {
    fun validate(item: WeightedLockerProfile): MutableList<String> {
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

    fun requireValid(item: WeightedLockerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLockerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLockerProfileValidator(): WeightedLockerProfileValidator {
    return WeightedLockerProfileValidator(
        mutableListOf<WeightedLockerProfileRule>(
            WeightedLockerProfileRequiredDockRule("weighted locker profile needs this field"),
            WeightedLockerProfileCodeLengthRule(1000),
            WeightedLockerProfileRequiredTitleRule("weighted locker profile needs this field"),
        ),
    )
}
