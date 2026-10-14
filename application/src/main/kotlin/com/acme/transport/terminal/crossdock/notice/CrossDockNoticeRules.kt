package com.acme.transport.terminal.crossdock.notice

/** A rule a cross dock notice must pass. Check returns its problems, or none. */
interface CrossDockNoticeRule {
    fun check(item: CrossDockNotice): MutableList<String>
}

data class CrossDockNoticeRequiredSourceRule(var message: String) : CrossDockNoticeRule {
    override fun check(item: CrossDockNotice): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockNoticeRequiredDriverIdRule(var message: String) : CrossDockNoticeRule {
    override fun check(item: CrossDockNotice): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockNoticeRequiredNameRule(var message: String) : CrossDockNoticeRule {
    override fun check(item: CrossDockNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock notice. */
data class CrossDockNoticeValidator(var rules: MutableList<CrossDockNoticeRule>) {
    fun validate(item: CrossDockNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockNoticeValidator(): CrossDockNoticeValidator {
    return CrossDockNoticeValidator(
        mutableListOf<CrossDockNoticeRule>(
            CrossDockNoticeRequiredSourceRule("cross dock notice needs a value"),
            CrossDockNoticeRequiredDriverIdRule("cross dock notice needs this field"),
            CrossDockNoticeRequiredNameRule("cross dock notice needs an entry"),
        ),
    )
}
