package com.acme.transport.network.cutoff.preference

/** A rule a cutoff preference must pass. Check returns its problems, or none. */
interface CutoffPreferenceRule {
    fun check(item: CutoffPreference): MutableList<String>
}

data class CutoffPreferenceRequiredDockRule(var message: String) : CutoffPreferenceRule {
    override fun check(item: CutoffPreference): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffPreferenceCodeLengthRule(var maxLength: Int) : CutoffPreferenceRule {
    override fun check(item: CutoffPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffPreferenceTitleLengthRule(var maxLength: Int) : CutoffPreferenceRule {
    override fun check(item: CutoffPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff preference. */
data class CutoffPreferenceValidator(var rules: MutableList<CutoffPreferenceRule>) {
    fun validate(item: CutoffPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffPreferenceValidator(): CutoffPreferenceValidator {
    return CutoffPreferenceValidator(
        mutableListOf<CutoffPreferenceRule>(
            CutoffPreferenceRequiredDockRule("cutoff preference needs a value"),
            CutoffPreferenceCodeLengthRule(80),
            CutoffPreferenceTitleLengthRule(64),
        ),
    )
}
