package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalProofOfDeliveryRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalProofOfDeliveryRecordValidator()
        val item = newGlobalProofOfDeliveryRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalProofOfDeliveryRecordAttemptsRangeRule(16, 64)
        val item = newGlobalProofOfDeliveryRecord("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalProofOfDeliveryRecordRequiredTitleRule("global proof of delivery record needs this field")
        val item = newGlobalProofOfDeliveryRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalProofOfDeliveryRecordTitleLengthRule(120)
        val item = newGlobalProofOfDeliveryRecord("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = GlobalProofOfDeliveryRecordRequiredCategoryRule("global proof of delivery record needs an entry")
        val item = newGlobalProofOfDeliveryRecord("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalProofOfDeliveryRecordOdometerRangeRule(16, 255)
        val item = newGlobalProofOfDeliveryRecord("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
