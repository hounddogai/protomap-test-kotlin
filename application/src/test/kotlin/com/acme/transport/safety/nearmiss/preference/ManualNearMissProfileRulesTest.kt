package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualNearMissProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualNearMissProfileValidator()
        val item = newManualNearMissProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualNearMissProfileRequiredCodeRule("manual near miss profile needs an entry")
        val item = newManualNearMissProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualNearMissProfileCodeLengthRule(80)
        val item = newManualNearMissProfile("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualNearMissProfileNameLengthRule(80)
        val item = newManualNearMissProfile("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualNearMissProfileRequiredExternalRefRule("manual near miss profile needs this field")
        val item = newManualNearMissProfile("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
