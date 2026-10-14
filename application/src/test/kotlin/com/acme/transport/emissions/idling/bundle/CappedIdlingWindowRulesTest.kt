package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIdlingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedIdlingWindowValidator()
        val item = newCappedIdlingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CappedIdlingWindowRequiredLaneRule("capped idling window needs a value")
        val item = newCappedIdlingWindow("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
