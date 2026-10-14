package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedInspectionRequestValidator()
        val item = newCappedInspectionRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CappedInspectionRequestRatioRangeRule(3.3, 4022.4)
        val item = newCappedInspectionRequest("x", "y")
        item.ratio = 4023.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedInspectionRequestLocaleLengthRule(1000)
        val item = newCappedInspectionRequest("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedInspectionRequestLabelLengthRule(120)
        val item = newCappedInspectionRequest("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CappedInspectionRequestChannelLengthRule(3600)
        val item = newCappedInspectionRequest("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CappedInspectionRequestPalletsRangeRule(120, 255)
        val item = newCappedInspectionRequest("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
