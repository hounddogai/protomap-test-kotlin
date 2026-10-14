package com.acme.transport.safety.cameraevent.forecast

/** A rule a tiered camera event target must pass. Check returns its problems, or none. */
interface TieredCameraEventTargetRule {
    fun check(item: TieredCameraEventTarget): MutableList<String>
}

data class TieredCameraEventTargetRequiredPhoneRule(var message: String) : TieredCameraEventTargetRule {
    override fun check(item: TieredCameraEventTarget): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventTargetLabelLengthRule(var maxLength: Int) : TieredCameraEventTargetRule {
    override fun check(item: TieredCameraEventTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventTargetTitleLengthRule(var maxLength: Int) : TieredCameraEventTargetRule {
    override fun check(item: TieredCameraEventTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered camera event target. */
data class TieredCameraEventTargetValidator(var rules: MutableList<TieredCameraEventTargetRule>) {
    fun validate(item: TieredCameraEventTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCameraEventTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCameraEventTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCameraEventTargetValidator(): TieredCameraEventTargetValidator {
    return TieredCameraEventTargetValidator(
        mutableListOf<TieredCameraEventTargetRule>(
            TieredCameraEventTargetRequiredPhoneRule("tiered camera event target needs an entry"),
            TieredCameraEventTargetLabelLengthRule(3600),
            TieredCameraEventTargetTitleLengthRule(64),
        ),
    )
}
