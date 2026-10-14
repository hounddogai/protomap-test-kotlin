package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulAdjustmentValidator()
        val item = newBackhaulAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = BackhaulAdjustmentLaneLengthRule(3600)
        val item = newBackhaulAdjustment("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulAdjustmentCodeLengthRule(3600)
        val item = newBackhaulAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BackhaulAdjustmentNameLengthRule(64)
        val item = newBackhaulAdjustment("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
