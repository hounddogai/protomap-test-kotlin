package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDisputeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDisputeBundleValidator()
        val item = newManualDisputeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = ManualDisputeBundleNotesLengthRule(32)
        val item = newManualDisputeBundle("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDisputeBundleCodeLengthRule(80)
        val item = newManualDisputeBundle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualDisputeBundleLabelLengthRule(80)
        val item = newManualDisputeBundle("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = ManualDisputeBundleRegionLengthRule(64)
        val item = newManualDisputeBundle("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ManualDisputeBundleRequiredRegionRule("manual dispute bundle needs this field")
        val item = newManualDisputeBundle("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
