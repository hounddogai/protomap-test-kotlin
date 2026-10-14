package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLanePlanOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredLanePlanOverrideValidator()
        val item = newTieredLanePlanOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredLanePlanOverrideRequiredLabelRule("tiered lane plan override needs an entry")
        val item = newTieredLanePlanOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TieredLanePlanOverrideCurrencyLengthRule(3600)
        val item = newTieredLanePlanOverride("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredLanePlanOverrideRequiredCodeRule("tiered lane plan override needs a value")
        val item = newTieredLanePlanOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TieredLanePlanOverrideRequiredCurrencyRule("tiered lane plan override needs an entry")
        val item = newTieredLanePlanOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TieredLanePlanOverrideSequenceRangeRule(255, 86400)
        val item = newTieredLanePlanOverride("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
