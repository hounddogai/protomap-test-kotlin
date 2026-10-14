package com.acme.transport.safety.cameraevent.recorditem

/** A rule a camera event draft must pass. Check returns its problems, or none. */
interface CameraEventDraftRule {
    fun check(item: CameraEventDraft): MutableList<String>
}

data class CameraEventDraftLabelLengthRule(var maxLength: Int) : CameraEventDraftRule {
    override fun check(item: CameraEventDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDraftMarginRangeRule(var min: Double, var max: Double) : CameraEventDraftRule {
    override fun check(item: CameraEventDraft): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CameraEventDraftRequiredCodeRule(var message: String) : CameraEventDraftRule {
    override fun check(item: CameraEventDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventDraftScoreRangeRule(var min: Double, var max: Double) : CameraEventDraftRule {
    override fun check(item: CameraEventDraft): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class CameraEventDraftRequiredNameRule(var message: String) : CameraEventDraftRule {
    override fun check(item: CameraEventDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event draft. */
data class CameraEventDraftValidator(var rules: MutableList<CameraEventDraftRule>) {
    fun validate(item: CameraEventDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventDraftValidator(): CameraEventDraftValidator {
    return CameraEventDraftValidator(
        mutableListOf<CameraEventDraftRule>(
            CameraEventDraftLabelLengthRule(32),
            CameraEventDraftMarginRangeRule(0.1, 2902.7),
            CameraEventDraftRequiredCodeRule("camera event draft needs an entry"),
            CameraEventDraftScoreRangeRule(9.4, 1828.7),
            CameraEventDraftRequiredNameRule("camera event draft needs a value"),
        ),
    )
}
