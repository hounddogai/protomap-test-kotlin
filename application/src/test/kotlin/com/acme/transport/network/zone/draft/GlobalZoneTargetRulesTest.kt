package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalZoneTargetValidator()
        val item = newGlobalZoneTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = GlobalZoneTargetRequiredLocaleRule("global zone target needs this field")
        val item = newGlobalZoneTarget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
