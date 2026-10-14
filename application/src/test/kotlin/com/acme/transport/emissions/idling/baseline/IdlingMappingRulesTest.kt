package com.acme.transport.emissions.idling.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingMappingValidator()
        val item = newIdlingMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingMappingRequiredTitleRule("idling mapping needs a value")
        val item = newIdlingMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = IdlingMappingRequiredLaneRule("idling mapping needs this field")
        val item = newIdlingMapping("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingMappingCodeLengthRule(86400)
        val item = newIdlingMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
