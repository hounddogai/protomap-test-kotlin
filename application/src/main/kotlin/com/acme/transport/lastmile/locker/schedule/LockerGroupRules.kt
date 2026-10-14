package com.acme.transport.lastmile.locker.schedule

/** A rule a locker group must pass. Check returns its problems, or none. */
interface LockerGroupRule {
    fun check(item: LockerGroup): MutableList<String>
}

data class LockerGroupTimezoneLengthRule(var maxLength: Int) : LockerGroupRule {
    override fun check(item: LockerGroup): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerGroupRequiredCarrierRule(var message: String) : LockerGroupRule {
    override fun check(item: LockerGroup): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerGroupTitleLengthRule(var maxLength: Int) : LockerGroupRule {
    override fun check(item: LockerGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerGroupCodeLengthRule(var maxLength: Int) : LockerGroupRule {
    override fun check(item: LockerGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker group. */
data class LockerGroupValidator(var rules: MutableList<LockerGroupRule>) {
    fun validate(item: LockerGroup): MutableList<String> {
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

    fun requireValid(item: LockerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerGroupValidator(): LockerGroupValidator {
    return LockerGroupValidator(
        mutableListOf<LockerGroupRule>(
            LockerGroupTimezoneLengthRule(64),
            LockerGroupRequiredCarrierRule("locker group needs this field"),
            LockerGroupTitleLengthRule(255),
            LockerGroupCodeLengthRule(255),
        ),
    )
}
