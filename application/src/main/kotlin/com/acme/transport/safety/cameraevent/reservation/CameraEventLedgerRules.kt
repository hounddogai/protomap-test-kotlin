package com.acme.transport.safety.cameraevent.reservation

/** A rule a camera event ledger must pass. Check returns its problems, or none. */
interface CameraEventLedgerRule {
    fun check(item: CameraEventLedger): MutableList<String>
}

data class CameraEventLedgerRequiredChecksumRule(var message: String) : CameraEventLedgerRule {
    override fun check(item: CameraEventLedger): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventLedgerRequiredDriverIdRule(var message: String) : CameraEventLedgerRule {
    override fun check(item: CameraEventLedger): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventLedgerCodeLengthRule(var maxLength: Int) : CameraEventLedgerRule {
    override fun check(item: CameraEventLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event ledger. */
data class CameraEventLedgerValidator(var rules: MutableList<CameraEventLedgerRule>) {
    fun validate(item: CameraEventLedger): MutableList<String> {
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

    fun requireValid(item: CameraEventLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventLedgerValidator(): CameraEventLedgerValidator {
    return CameraEventLedgerValidator(
        mutableListOf<CameraEventLedgerRule>(
            CameraEventLedgerRequiredChecksumRule("camera event ledger needs a value"),
            CameraEventLedgerRequiredDriverIdRule("camera event ledger needs an entry"),
            CameraEventLedgerCodeLengthRule(32),
        ),
    )
}
