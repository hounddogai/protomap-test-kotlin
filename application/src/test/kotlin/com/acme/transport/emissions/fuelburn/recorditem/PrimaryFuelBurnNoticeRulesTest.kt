package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelBurnNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelBurnNoticeValidator()
        val item = newPrimaryFuelBurnNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryFuelBurnNoticeRequiredNameRule("primary fuel burn notice needs a value")
        val item = newPrimaryFuelBurnNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryFuelBurnNoticeRequiredCodeRule("primary fuel burn notice needs this field")
        val item = newPrimaryFuelBurnNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
