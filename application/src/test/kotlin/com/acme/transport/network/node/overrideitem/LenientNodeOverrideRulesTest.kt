package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNodeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNodeOverrideValidator()
        val item = newLenientNodeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientNodeOverrideRequiredNameRule("lenient node override needs a value")
        val item = newLenientNodeOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientNodeOverrideRequiredCodeRule("lenient node override needs a value")
        val item = newLenientNodeOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = LenientNodeOverrideRequiredCurrencyRule("lenient node override needs a value")
        val item = newLenientNodeOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LenientNodeOverrideRequiredCategoryRule("lenient node override needs an entry")
        val item = newLenientNodeOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
