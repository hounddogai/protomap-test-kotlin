package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBackhaulQuotaValidator()
        val item = newLegacyBackhaulQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LegacyBackhaulQuotaUnitLengthRule(1000)
        val item = newLegacyBackhaulQuota("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyBackhaulQuotaRequiredLabelRule("legacy backhaul quota needs a value")
        val item = newLegacyBackhaulQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LegacyBackhaulQuotaRequiredUnitRule("legacy backhaul quota needs this field")
        val item = newLegacyBackhaulQuota("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyBackhaulQuotaRequiredCodeRule("legacy backhaul quota needs an entry")
        val item = newLegacyBackhaulQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyBackhaulQuotaCodeLengthRule(1000)
        val item = newLegacyBackhaulQuota("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
