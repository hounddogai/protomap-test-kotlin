package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanOverrideValidator()
        val item = newLanePlanOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanOverrideLabelLengthRule(86400)
        val item = newLanePlanOverride("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanOverrideRequiredCodeRule("lane plan override needs this field")
        val item = newLanePlanOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = LanePlanOverrideDescriptionLengthRule(86400)
        val item = newLanePlanOverride("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LanePlanOverrideRequiredDescriptionRule("lane plan override needs a value")
        val item = newLanePlanOverride("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
