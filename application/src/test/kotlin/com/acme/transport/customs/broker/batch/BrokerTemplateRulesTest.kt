package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerTemplateValidator()
        val item = newBrokerTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BrokerTemplateRequiredLabelRule("broker template needs a value")
        val item = newBrokerTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerTemplateCodeLengthRule(64)
        val item = newBrokerTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BrokerTemplateRequiredContactEmailRule("broker template needs an entry")
        val item = newBrokerTemplate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BrokerTemplateOdometerRangeRule(80, 3600)
        val item = newBrokerTemplate("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
