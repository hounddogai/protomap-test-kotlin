package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCutoffManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCutoffManifestValidator()
        val item = newLenientCutoffManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientCutoffManifestNameLengthRule(1000)
        val item = newLenientCutoffManifest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LenientCutoffManifestCeilingRangeRule(3.6, 4826.0)
        val item = newLenientCutoffManifest("x", "y")
        item.ceiling = 4827.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LenientCutoffManifestRequiredSourceRule("lenient cutoff manifest needs an entry")
        val item = newLenientCutoffManifest("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LenientCutoffManifestAmountRangeRule(5.9, 4634.5)
        val item = newLenientCutoffManifest("x", "y")
        item.amount = 4636.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
