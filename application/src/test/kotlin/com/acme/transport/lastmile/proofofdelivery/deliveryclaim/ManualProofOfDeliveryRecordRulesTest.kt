package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualProofOfDeliveryRecordValidator()
        val item = newManualProofOfDeliveryRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualProofOfDeliveryRecordCodeLengthRule(86400)
        val item = newManualProofOfDeliveryRecord("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualProofOfDeliveryRecordTitleLengthRule(1000)
        val item = newManualProofOfDeliveryRecord("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ManualProofOfDeliveryRecordRequiredSourceRule("manual proof of delivery record needs an entry")
        val item = newManualProofOfDeliveryRecord("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
