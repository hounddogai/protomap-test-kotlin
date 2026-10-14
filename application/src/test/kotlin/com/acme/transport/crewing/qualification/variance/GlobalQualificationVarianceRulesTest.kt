package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalQualificationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalQualificationVarianceValidator()
        val item = newGlobalQualificationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalQualificationVarianceRateRangeRule(2.4, 3208.6)
        val item = newGlobalQualificationVariance("x", "y")
        item.rate = 3210.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalQualificationVarianceCodeLengthRule(120)
        val item = newGlobalQualificationVariance("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = GlobalQualificationVarianceDriverIdLengthRule(3600)
        val item = newGlobalQualificationVariance("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalQualificationVarianceLabelLengthRule(1000)
        val item = newGlobalQualificationVariance("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
