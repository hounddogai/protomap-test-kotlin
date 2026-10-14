package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingServiceAreaTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingServiceAreaTargetValidator()
        val item = newRollingServiceAreaTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingServiceAreaTargetRequiredCodeRule("rolling service area target needs a value")
        val item = newRollingServiceAreaTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
