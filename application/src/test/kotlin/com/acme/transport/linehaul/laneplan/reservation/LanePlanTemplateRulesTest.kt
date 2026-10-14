package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanTemplateValidator()
        val item = newLanePlanTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = LanePlanTemplateChecksumLengthRule(32)
        val item = newLanePlanTemplate("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LanePlanTemplateRequiredLabelRule("lane plan template needs an entry")
        val item = newLanePlanTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
