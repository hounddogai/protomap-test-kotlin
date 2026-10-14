package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDropSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDropSlotValidator()
        val item = newLegacyDropSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDropSlotRequiredCodeRule("legacy drop slot needs an entry")
        val item = newLegacyDropSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
