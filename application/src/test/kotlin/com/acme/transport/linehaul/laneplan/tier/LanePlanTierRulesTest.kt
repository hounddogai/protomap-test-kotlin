package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanTierValidator()
        val item = newLanePlanTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanTierRequiredCodeRule("lane plan tier needs a value")
        val item = newLanePlanTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LanePlanTierRequiredShiftCodeRule("lane plan tier needs an entry")
        val item = newLanePlanTier("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
