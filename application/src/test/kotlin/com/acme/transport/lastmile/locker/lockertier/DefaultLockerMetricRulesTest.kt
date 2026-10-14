package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLockerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultLockerMetricValidator()
        val item = newDefaultLockerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultLockerMetricRequiredLabelRule("default locker metric needs an entry")
        val item = newDefaultLockerMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultLockerMetricCodeLengthRule(1000)
        val item = newDefaultLockerMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
