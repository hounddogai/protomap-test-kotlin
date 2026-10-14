package com.acme.transport.lastmile.locker.manifest

/** A rule a locker manifest must pass. Check returns its problems, or none. */
interface LockerManifestRule {
    fun check(item: LockerManifest): MutableList<String>
}

data class LockerManifestStartsAtRangeRule(var min: Int, var max: Int) : LockerManifestRule {
    override fun check(item: LockerManifest): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerManifestRequiredCodeRule(var message: String) : LockerManifestRule {
    override fun check(item: LockerManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerManifestNameLengthRule(var maxLength: Int) : LockerManifestRule {
    override fun check(item: LockerManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker manifest. */
data class LockerManifestValidator(var rules: MutableList<LockerManifestRule>) {
    fun validate(item: LockerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerManifestValidator(): LockerManifestValidator {
    return LockerManifestValidator(
        mutableListOf<LockerManifestRule>(
            LockerManifestStartsAtRangeRule(64, 120),
            LockerManifestRequiredCodeRule("locker manifest needs a value"),
            LockerManifestNameLengthRule(32),
        ),
    )
}
