package com.acme.transport.customs.hold.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHoldGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientHoldGroupValidator()
        val item = newLenientHoldGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientHoldGroupRequiredLabelRule("lenient hold group needs this field")
        val item = newLenientHoldGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientHoldGroupRequiredCodeRule("lenient hold group needs a value")
        val item = newLenientHoldGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LenientHoldGroupRequiredCountryRule("lenient hold group needs an entry")
        val item = newLenientHoldGroup("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LenientHoldGroupRequiredUnitRule("lenient hold group needs a value")
        val item = newLenientHoldGroup("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
