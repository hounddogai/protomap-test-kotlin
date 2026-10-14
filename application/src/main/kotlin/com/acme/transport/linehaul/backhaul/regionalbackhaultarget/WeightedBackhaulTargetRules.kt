package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

/** A rule a weighted backhaul target must pass. Check returns its problems, or none. */
interface WeightedBackhaulTargetRule {
    fun check(item: WeightedBackhaulTarget): MutableList<String>
}

data class WeightedBackhaulTargetRequiredCodeRule(var message: String) : WeightedBackhaulTargetRule {
    override fun check(item: WeightedBackhaulTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulTargetRequiredNameRule(var message: String) : WeightedBackhaulTargetRule {
    override fun check(item: WeightedBackhaulTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulTargetCodeLengthRule(var maxLength: Int) : WeightedBackhaulTargetRule {
    override fun check(item: WeightedBackhaulTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulTargetPinnedGuardRule(var limit: Int) : WeightedBackhaulTargetRule {
    override fun check(item: WeightedBackhaulTarget): MutableList<String> {
        if (item.pinned && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulTargetSourceLengthRule(var maxLength: Int) : WeightedBackhaulTargetRule {
    override fun check(item: WeightedBackhaulTarget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted backhaul target. */
data class WeightedBackhaulTargetValidator(var rules: MutableList<WeightedBackhaulTargetRule>) {
    fun validate(item: WeightedBackhaulTarget): MutableList<String> {
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

    fun requireValid(item: WeightedBackhaulTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBackhaulTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBackhaulTargetValidator(): WeightedBackhaulTargetValidator {
    return WeightedBackhaulTargetValidator(
        mutableListOf<WeightedBackhaulTargetRule>(
            WeightedBackhaulTargetRequiredCodeRule("weighted backhaul target needs an entry"),
            WeightedBackhaulTargetRequiredNameRule("weighted backhaul target needs this field"),
            WeightedBackhaulTargetCodeLengthRule(32),
            WeightedBackhaulTargetPinnedGuardRule(86400),
            WeightedBackhaulTargetSourceLengthRule(64),
        ),
    )
}
