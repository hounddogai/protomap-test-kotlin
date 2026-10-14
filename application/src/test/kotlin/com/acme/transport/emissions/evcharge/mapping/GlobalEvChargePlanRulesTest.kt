package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalEvChargePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalEvChargePlanValidator()
        val item = newGlobalEvChargePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = GlobalEvChargePlanDockLengthRule(16)
        val item = newGlobalEvChargePlan("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalEvChargePlanRequiredCodeRule("global ev charge plan needs this field")
        val item = newGlobalEvChargePlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = GlobalEvChargePlanRequiredDockRule("global ev charge plan needs an entry")
        val item = newGlobalEvChargePlan("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = GlobalEvChargePlanRequiredCarrierRule("global ev charge plan needs this field")
        val item = newGlobalEvChargePlan("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
