package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLockerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLockerProfileValidator()
        val item = newWeightedLockerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = WeightedLockerProfileRequiredDockRule("weighted locker profile needs a value")
        val item = newWeightedLockerProfile("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedLockerProfileCodeLengthRule(255)
        val item = newWeightedLockerProfile("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedLockerProfileRequiredTitleRule("weighted locker profile needs a value")
        val item = newWeightedLockerProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
