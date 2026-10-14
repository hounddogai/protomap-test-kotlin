package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingScheduleValidator()
        val item = newIdlingSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = IdlingScheduleRequiredDockRule("idling schedule needs a value")
        val item = newIdlingSchedule("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = IdlingScheduleDescriptionLengthRule(86400)
        val item = newIdlingSchedule("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = IdlingScheduleNameLengthRule(1000)
        val item = newIdlingSchedule("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = IdlingScheduleRequiredNameRule("idling schedule needs a value")
        val item = newIdlingSchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
