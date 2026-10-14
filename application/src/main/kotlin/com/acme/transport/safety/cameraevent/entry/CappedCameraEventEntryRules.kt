package com.acme.transport.safety.cameraevent.entry

/** A rule a capped camera event entry must pass. Check returns its problems, or none. */
interface CappedCameraEventEntryRule {
    fun check(item: CappedCameraEventEntry): MutableList<String>
}

data class CappedCameraEventEntryNameLengthRule(var maxLength: Int) : CappedCameraEventEntryRule {
    override fun check(item: CappedCameraEventEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCameraEventEntryEtaMinutesRangeRule(var min: Int, var max: Int) : CappedCameraEventEntryRule {
    override fun check(item: CappedCameraEventEntry): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCameraEventEntryRequiredNameRule(var message: String) : CappedCameraEventEntryRule {
    override fun check(item: CappedCameraEventEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped camera event entry. */
data class CappedCameraEventEntryValidator(var rules: MutableList<CappedCameraEventEntryRule>) {
    fun validate(item: CappedCameraEventEntry): MutableList<String> {
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

    fun requireValid(item: CappedCameraEventEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCameraEventEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCameraEventEntryValidator(): CappedCameraEventEntryValidator {
    return CappedCameraEventEntryValidator(
        mutableListOf<CappedCameraEventEntryRule>(
            CappedCameraEventEntryNameLengthRule(80),
            CappedCameraEventEntryEtaMinutesRangeRule(255, 3600),
            CappedCameraEventEntryRequiredNameRule("capped camera event entry needs a value"),
        ),
    )
}
