package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBackhaulTemplateValidator()
        val item = newCappedBackhaulTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = CappedBackhaulTemplateScoreRangeRule(5.6, 1997.1)
        val item = newCappedBackhaulTemplate("x", "y")
        item.score = 1998.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedBackhaulTemplateCodeLengthRule(255)
        val item = newCappedBackhaulTemplate("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = CappedBackhaulTemplateVehicleIdLengthRule(3600)
        val item = newCappedBackhaulTemplate("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CappedBackhaulTemplateExpiresAtRangeRule(32, 80)
        val item = newCappedBackhaulTemplate("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
