package com.acme.transport.freightpay.accessorial.plan

/** A rule a legacy accessorial draft must pass. Check returns its problems, or none. */
interface LegacyAccessorialDraftRule {
    fun check(item: LegacyAccessorialDraft): MutableList<String>
}

data class LegacyAccessorialDraftRequiredNotesRule(var message: String) : LegacyAccessorialDraftRule {
    override fun check(item: LegacyAccessorialDraft): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialDraftRequiredCodeRule(var message: String) : LegacyAccessorialDraftRule {
    override fun check(item: LegacyAccessorialDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialDraftCodeLengthRule(var maxLength: Int) : LegacyAccessorialDraftRule {
    override fun check(item: LegacyAccessorialDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialDraftTitleLengthRule(var maxLength: Int) : LegacyAccessorialDraftRule {
    override fun check(item: LegacyAccessorialDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialDraftNotesLengthRule(var maxLength: Int) : LegacyAccessorialDraftRule {
    override fun check(item: LegacyAccessorialDraft): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy accessorial draft. */
data class LegacyAccessorialDraftValidator(var rules: MutableList<LegacyAccessorialDraftRule>) {
    fun validate(item: LegacyAccessorialDraft): MutableList<String> {
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

    fun requireValid(item: LegacyAccessorialDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAccessorialDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAccessorialDraftValidator(): LegacyAccessorialDraftValidator {
    return LegacyAccessorialDraftValidator(
        mutableListOf<LegacyAccessorialDraftRule>(
            LegacyAccessorialDraftRequiredNotesRule("legacy accessorial draft needs an entry"),
            LegacyAccessorialDraftRequiredCodeRule("legacy accessorial draft needs a value"),
            LegacyAccessorialDraftCodeLengthRule(80),
            LegacyAccessorialDraftTitleLengthRule(80),
            LegacyAccessorialDraftNotesLengthRule(86400),
        ),
    )
}
