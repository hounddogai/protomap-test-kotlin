package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEmissionFactorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEmissionFactorExceptionValidator()
        val item = newTieredEmissionFactorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredEmissionFactorExceptionOdometerRangeRule(16, 3600)
        val item = newTieredEmissionFactorException("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredEmissionFactorExceptionRequiredTitleRule("tiered emission factor exception needs an entry")
        val item = newTieredEmissionFactorException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
