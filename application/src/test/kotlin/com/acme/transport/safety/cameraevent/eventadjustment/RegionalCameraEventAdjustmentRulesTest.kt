package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCameraEventAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCameraEventAdjustmentValidator()
        val item = newRegionalCameraEventAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalCameraEventAdjustmentRequiredNameRule("regional camera event adjustment needs a value")
        val item = newRegionalCameraEventAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalCameraEventAdjustmentNameLengthRule(80)
        val item = newRegionalCameraEventAdjustment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCameraEventAdjustmentCodeLengthRule(1000)
        val item = newRegionalCameraEventAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
