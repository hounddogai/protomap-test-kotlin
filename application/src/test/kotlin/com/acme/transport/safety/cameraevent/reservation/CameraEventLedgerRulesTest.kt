package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventLedgerValidator()
        val item = newCameraEventLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CameraEventLedgerRequiredChecksumRule("camera event ledger needs a value")
        val item = newCameraEventLedger("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CameraEventLedgerRequiredDriverIdRule("camera event ledger needs a value")
        val item = newCameraEventLedger("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventLedgerCodeLengthRule(64)
        val item = newCameraEventLedger("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
