package com.acme.transport.safety.cameraevent.batch

/** A rule a camera event target must pass. Check returns its problems, or none. */
interface CameraEventTargetRule {
    fun check(item: CameraEventTarget): MutableList<String>
}

data class CameraEventTargetRequiredCodeRule(var message: String) : CameraEventTargetRule {
    override fun check(item: CameraEventTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTargetRequiredLabelRule(var message: String) : CameraEventTargetRule {
    override fun check(item: CameraEventTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventTargetLabelLengthRule(var maxLength: Int) : CameraEventTargetRule {
    override fun check(item: CameraEventTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event target. */
data class CameraEventTargetValidator(var rules: MutableList<CameraEventTargetRule>) {
    fun validate(item: CameraEventTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventTargetValidator(): CameraEventTargetValidator {
    return CameraEventTargetValidator(
        mutableListOf<CameraEventTargetRule>(
            CameraEventTargetRequiredCodeRule("camera event target needs this field"),
            CameraEventTargetRequiredLabelRule("camera event target needs an entry"),
            CameraEventTargetLabelLengthRule(16),
        ),
    )
}
