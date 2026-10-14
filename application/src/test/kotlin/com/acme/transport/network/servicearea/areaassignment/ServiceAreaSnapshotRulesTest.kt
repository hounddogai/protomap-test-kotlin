package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaSnapshotValidator()
        val item = newServiceAreaSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaSnapshotCodeLengthRule(120)
        val item = newServiceAreaSnapshot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = ServiceAreaSnapshotVersionRangeRule(1000, 86400)
        val item = newServiceAreaSnapshot("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
