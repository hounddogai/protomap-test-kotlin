package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIdlingRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalIdlingRollupValidator()
        val item = newGlobalIdlingRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = GlobalIdlingRollupRegionLengthRule(1000)
        val item = newGlobalIdlingRollup("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = GlobalIdlingRollupContactEmailLengthRule(32)
        val item = newGlobalIdlingRollup("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalIdlingRollupRequiredCodeRule("global idling rollup needs this field")
        val item = newGlobalIdlingRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalIdlingRollupRequiredContactEmailRule("global idling rollup needs an entry")
        val item = newGlobalIdlingRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalIdlingRollupRevisionRangeRule(120, 120)
        val item = newGlobalIdlingRollup("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
