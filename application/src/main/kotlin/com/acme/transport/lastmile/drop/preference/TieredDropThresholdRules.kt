package com.acme.transport.lastmile.drop.preference

/** A rule a tiered drop threshold must pass. Check returns its problems, or none. */
interface TieredDropThresholdRule {
    fun check(item: TieredDropThreshold): MutableList<String>
}

data class TieredDropThresholdRequiredShiftCodeRule(var message: String) : TieredDropThresholdRule {
    override fun check(item: TieredDropThreshold): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDropThresholdNameLengthRule(var maxLength: Int) : TieredDropThresholdRule {
    override fun check(item: TieredDropThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropThresholdCarrierLengthRule(var maxLength: Int) : TieredDropThresholdRule {
    override fun check(item: TieredDropThreshold): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropThresholdShiftCodeLengthRule(var maxLength: Int) : TieredDropThresholdRule {
    override fun check(item: TieredDropThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered drop threshold. */
data class TieredDropThresholdValidator(var rules: MutableList<TieredDropThresholdRule>) {
    fun validate(item: TieredDropThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDropThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDropThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDropThresholdValidator(): TieredDropThresholdValidator {
    return TieredDropThresholdValidator(
        mutableListOf<TieredDropThresholdRule>(
            TieredDropThresholdRequiredShiftCodeRule("tiered drop threshold needs a value"),
            TieredDropThresholdNameLengthRule(64),
            TieredDropThresholdCarrierLengthRule(80),
            TieredDropThresholdShiftCodeLengthRule(3600),
        ),
    )
}
