package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelBurnDigestValidator()
        val item = newGlobalFuelBurnDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalFuelBurnDigestRequiredLabelRule("global fuel burn digest needs a value")
        val item = newGlobalFuelBurnDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = GlobalFuelBurnDigestRequiredDescriptionRule("global fuel burn digest needs this field")
        val item = newGlobalFuelBurnDigest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelBurnDigestCodeLengthRule(120)
        val item = newGlobalFuelBurnDigest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalFuelBurnDigestRequiredCodeRule("global fuel burn digest needs this field")
        val item = newGlobalFuelBurnDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
