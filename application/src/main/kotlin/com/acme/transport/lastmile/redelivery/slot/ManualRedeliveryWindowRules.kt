package com.acme.transport.lastmile.redelivery.slot

/** A rule a manual redelivery window must pass. Check returns its problems, or none. */
interface ManualRedeliveryWindowRule {
    fun check(item: ManualRedeliveryWindow): MutableList<String>
}

data class ManualRedeliveryWindowNameLengthRule(var maxLength: Int) : ManualRedeliveryWindowRule {
    override fun check(item: ManualRedeliveryWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryWindowRequiredRegionRule(var message: String) : ManualRedeliveryWindowRule {
    override fun check(item: ManualRedeliveryWindow): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryWindowCodeLengthRule(var maxLength: Int) : ManualRedeliveryWindowRule {
    override fun check(item: ManualRedeliveryWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryWindowRequiredCodeRule(var message: String) : ManualRedeliveryWindowRule {
    override fun check(item: ManualRedeliveryWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual redelivery window. */
data class ManualRedeliveryWindowValidator(var rules: MutableList<ManualRedeliveryWindowRule>) {
    fun validate(item: ManualRedeliveryWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRedeliveryWindowValidator(): ManualRedeliveryWindowValidator {
    return ManualRedeliveryWindowValidator(
        mutableListOf<ManualRedeliveryWindowRule>(
            ManualRedeliveryWindowNameLengthRule(86400),
            ManualRedeliveryWindowRequiredRegionRule("manual redelivery window needs this field"),
            ManualRedeliveryWindowCodeLengthRule(64),
            ManualRedeliveryWindowRequiredCodeRule("manual redelivery window needs an entry"),
        ),
    )
}
