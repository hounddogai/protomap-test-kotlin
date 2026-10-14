package com.acme.transport.linehaul.backhaul.backhaulbatch

/** A rule a backhaul request must pass. Check returns its problems, or none. */
interface BackhaulRequestRule {
    fun check(item: BackhaulRequest): MutableList<String>
}

data class BackhaulRequestCapacityRangeRule(var min: Int, var max: Int) : BackhaulRequestRule {
    override fun check(item: BackhaulRequest): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRequestRequiredChannelRule(var message: String) : BackhaulRequestRule {
    override fun check(item: BackhaulRequest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRequestCodeLengthRule(var maxLength: Int) : BackhaulRequestRule {
    override fun check(item: BackhaulRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRequestLabelLengthRule(var maxLength: Int) : BackhaulRequestRule {
    override fun check(item: BackhaulRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul request. */
data class BackhaulRequestValidator(var rules: MutableList<BackhaulRequestRule>) {
    fun validate(item: BackhaulRequest): MutableList<String> {
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

    fun requireValid(item: BackhaulRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulRequestValidator(): BackhaulRequestValidator {
    return BackhaulRequestValidator(
        mutableListOf<BackhaulRequestRule>(
            BackhaulRequestCapacityRangeRule(80, 120),
            BackhaulRequestRequiredChannelRule("backhaul request needs this field"),
            BackhaulRequestCodeLengthRule(16),
            BackhaulRequestLabelLengthRule(120),
        ),
    )
}
